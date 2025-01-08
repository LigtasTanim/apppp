package com.example.ligtastanim;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class CropsHistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private CropsHistoryAdapter adapter;
    private List<CropHistoryItem> cropHistoryList;
    private String phoneNumber;
    private String selectedFarmingActivity;
    private String uid;
    private Spinner seasonSpinner;
    private TextView seasonStats;
    private static final String MAIN_CROP = "Main Crop Season";
    private static final String SECOND_CROP = "Second Crop Season";
    private static final String OFF_SEASON = "Off Season";
    private static final int YEARS_TO_SHOW = 3;

    private static class SeasonInfo {
        final int year;
        final String season;

        SeasonInfo(int year, String season) {
            this.year = year;
            this.season = season;
        }

        @Override
        public String toString() {
            return year + " - " + season;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SeasonInfo)) return false;
            SeasonInfo that = (SeasonInfo) o;
            return year == that.year && season.equals(that.season);
        }

        @Override
        public int hashCode() {
            return Objects.hash(year, season);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crops_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        uid = getIntent().getStringExtra("uniqueIdentifier");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        selectedFarmingActivity = getIntent().getStringExtra("FarmingActivity");

        seasonSpinner = findViewById(R.id.seasonSpinner);
        seasonStats = findViewById(R.id.seasonStats);

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Phone number is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("CropsHistoryActivity", "UID: " + uid);
        Log.d("CropsHistoryActivity", "Phone Number: " + phoneNumber);
        Log.d("CropsHistoryActivity", "Farming Activity: " + selectedFarmingActivity);

        cropHistoryList = new ArrayList<>();
        adapter = new CropsHistoryAdapter(this, cropHistoryList);
        historyRecyclerView.setAdapter(adapter);

        initializeViews();
        fetchCropHistory();
    }

    private void addSeasonalStatistics(List<CropHistoryItem> items) {
    if (items.isEmpty()) {
        return;
    }

    double totalArea = 0;
    double totalCosts = 0;
    int totalCrops = items.size();

    for (CropHistoryItem item : items) {
        totalArea += item.getFarmSize();
        totalCosts += item.getHarvestingRentAmount() +
                     item.getHarvestingGasMoney() +
                     item.getFertilizerCost() +
                     item.getWateringGasMoney() +
                     item.getWateringRentAmount();
    }


}

    private void initializeViews() {
        List<SeasonInfo> availableSeasons = generateAllSeasons();

        ArrayAdapter<SeasonInfo> spinnerAdapter = new ArrayAdapter<SeasonInfo>(
            this,
            android.R.layout.simple_spinner_item,
            availableSeasons) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                SeasonInfo info = getItem(position);
                view.setText(formatSeasonDisplay(info));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                SeasonInfo info = getItem(position);
                view.setText(formatSeasonDisplay(info));
                return view;
            }
        };

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seasonSpinner.setAdapter(spinnerAdapter);

        SeasonInfo currentSeason = getCurrentSeason();
        int currentSeasonIndex = availableSeasons.indexOf(currentSeason);
        if (currentSeasonIndex != -1) {
            seasonSpinner.setSelection(currentSeasonIndex);
        }

        seasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SeasonInfo selectedSeason = (SeasonInfo) parent.getItemAtPosition(position);
                filterCropsBySeason(selectedSeason);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private List<SeasonInfo> generateAllSeasons() {
        List<SeasonInfo> seasons = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);

        for (int year = currentYear; year > currentYear - YEARS_TO_SHOW; year--) {
            seasons.add(new SeasonInfo(year, MAIN_CROP));
            seasons.add(new SeasonInfo(year, SECOND_CROP));
            seasons.add(new SeasonInfo(year, OFF_SEASON));
        }

        return seasons;
    }

    private SeasonInfo getCurrentSeason() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String season = determineSeasonFromDate(new SimpleDateFormat("MM-dd-yyyy", Locale.US)
            .format(cal.getTime()));
        return new SeasonInfo(year, season);
    }

    private String formatSeasonDisplay(SeasonInfo info) {
        String seasonDisplay = info.season.replace(" Season", "");
        return info.year + " - " + seasonDisplay;
    }

    private void updateSeasonalDisplay(SeasonInfo selectedSeason, List<CropHistoryItem> items) {
        List<CropHistoryItem> seasonalItems = items.stream()
            .filter(item -> {
                try {
                    Date itemDate = new SimpleDateFormat("MM-dd-yyyy", Locale.US)
                        .parse(item.getStartDate());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(itemDate);
                    int itemYear = cal.get(Calendar.YEAR);
                    String itemSeason = determineSeasonFromDate(item.getStartDate());
                    return itemYear == selectedSeason.year &&
                           itemSeason.equals(selectedSeason.season);
                } catch (ParseException e) {
                    return false;
                }
            })
            .collect(Collectors.toList());

        cropHistoryList.clear();
        if (!seasonalItems.isEmpty()) {
            cropHistoryList.add(new CropHistoryItem(selectedSeason.season));
            addSeasonalStatistics(seasonalItems);
            cropHistoryList.addAll(seasonalItems);
        }
        adapter.notifyDataSetChanged();

        updateSeasonStats(seasonalItems);
    }

    private void updateSeasonStats(List<CropHistoryItem> items) {
        if (items.isEmpty()) {
            seasonStats.setText("No crops recorded for this season");
            return;
        }

        double totalArea = 0;
        double totalCosts = 0;

        for (CropHistoryItem item : items) {
            totalArea += item.getFarmSize();
            totalCosts += item.getHarvestingRentAmount() +
                         item.getHarvestingGasMoney() +
                         item.getFertilizerCost() +
                         item.getWateringGasMoney() +
                         item.getWateringRentAmount();
        }

        String stats = String.format(Locale.US,
            "Total Crops: %d\nTotal Area: %.2f hectares\nTotal Costs: ₱%.2f",
            items.size(), totalArea, totalCosts);
        seasonStats.setText(stats);
    }

    private void fetchCropHistory() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("CropsMonitoring");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<CropHistoryItem> allCrops = new ArrayList<>();

                for (DataSnapshot farmingActivitySnapshot : dataSnapshot.getChildren()) {
                    String farmingActivity = farmingActivitySnapshot.getKey();

                    for (DataSnapshot uniqueIdentifierSnapshot : farmingActivitySnapshot.getChildren()) {
                        DataSnapshot phoneSnapshot = uniqueIdentifierSnapshot.child(phoneNumber);

                        if (phoneSnapshot.exists()) {
                            String mainStatus = phoneSnapshot.child("MainStatus").getValue(String.class);
                            String startDate = phoneSnapshot.child("startDate").getValue(String.class);

                            if ("completed".equals(mainStatus) && startDate != null) {
                                double harvestingRentAmount = parseDoubleSafe(phoneSnapshot.child("harvestingRentAmount").getValue(String.class));
                                double harvestingGasMoney = parseDoubleSafe(phoneSnapshot.child("harvestingGasMoney").getValue(String.class));
                                double fertilizerCost = parseDoubleSafe(phoneSnapshot.child("fertilizerCost").getValue(String.class));
                                double farmSize = parseDoubleSafe(phoneSnapshot.child("farmSize").getValue(String.class));
                                double wateringGasMoney = parseDoubleSafe(phoneSnapshot.child("wateringGasMoney").getValue(String.class));
                                double wateringRentAmount = parseDoubleSafe(phoneSnapshot.child("wateringRentAmount").getValue(String.class));

                                allCrops.add(new CropHistoryItem(farmingActivity, startDate, harvestingRentAmount,
                                    harvestingGasMoney, fertilizerCost, farmSize, wateringGasMoney, wateringRentAmount));
                            }
                        }
                    }
                }

                SeasonInfo selectedSeason = (SeasonInfo) seasonSpinner.getSelectedItem();
                filterCropsBySeason(selectedSeason, allCrops);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CropsHistoryActivity", "Failed to read crop history", databaseError.toException());
                Toast.makeText(CropsHistoryActivity.this, "Failed to fetch crop history.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterCropsBySeason(SeasonInfo selectedSeason) {
        filterCropsBySeason(selectedSeason, new ArrayList<>(cropHistoryList));
    }

    private void filterCropsBySeason(SeasonInfo selectedSeason, List<CropHistoryItem> allCrops) {
        if (selectedSeason == null || allCrops == null) {
            return;
        }

        List<CropHistoryItem> filteredCrops = allCrops.stream()
            .filter(item -> {
                if (item == null || item.getStartDate() == null || item.getStartDate().isEmpty()) {
                    return false;
                }

                try {
                    Date itemDate = new SimpleDateFormat("MM-dd-yyyy", Locale.US).parse(item.getStartDate());
                    if (itemDate == null) {
                        return false;
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(itemDate);
                    int itemYear = cal.get(Calendar.YEAR);
                    String itemSeason = determineSeasonFromDate(item.getStartDate());
                    
                    return itemYear == selectedSeason.year && 
                           itemSeason != null && 
                           itemSeason.equals(selectedSeason.season);
                } catch (ParseException e) {
                    Log.e("CropsHistoryActivity", "Error parsing date: " + item.getStartDate(), e);
                    return false;
                }
            })
            .collect(Collectors.toList());

        cropHistoryList.clear();
        
        if (!filteredCrops.isEmpty()) {
            cropHistoryList.add(new CropHistoryItem(selectedSeason.season));
            addSeasonalStatistics(filteredCrops);
            cropHistoryList.addAll(filteredCrops);
        } else {
            CropHistoryItem emptyItem = new CropHistoryItem("No crops found for " + 
                selectedSeason.year + " " + selectedSeason.season);
            emptyItem.setIsEmpty(true);
            cropHistoryList.add(emptyItem);
        }
        
        adapter.notifyDataSetChanged();
        
        updateSeasonStats(filteredCrops);
    }

    private double parseDoubleSafe(String value) {
        try {
            return value != null && !value.isEmpty() ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String determineSeasonFromDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return OFF_SEASON; 
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            Date date = sdf.parse(dateStr);
            if (date == null) {
                return OFF_SEASON;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int month = cal.get(Calendar.MONTH) + 1; 
            int day = cal.get(Calendar.DAY_OF_MONTH);

            if ((month == 9 && day >= 16) || month > 9 || month == 1) {
                return MAIN_CROP;
            }
            else if (month >= 2 && month <= 4) {
                return SECOND_CROP;
            }
            else {
                return OFF_SEASON;
            }
        } catch (ParseException e) {
            Log.e("CropsHistoryActivity", "Error parsing date: " + dateStr, e);
            return OFF_SEASON; 
        }
    }

}
