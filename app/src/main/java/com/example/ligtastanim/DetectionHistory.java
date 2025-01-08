package com.example.ligtastanim;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DetectionHistory extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DetectionHistoryAdapter adapter;
    private TextView emptyView;
    private DatabaseReference mDatabase;
    private String phoneNumber;
    private static final String TAG = "DetectionHistory";
    private Spinner seasonSpinner;
    private TextView seasonStats;
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

    private SeasonInfo getSeasonInfo(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        
        String season;
        if (month == Calendar.SEPTEMBER && day >= 16) {
            season = "Main Crop Season";
        } else if (month >= Calendar.OCTOBER || month <= Calendar.JANUARY) {
            season = "Main Crop Season";
            if (month <= Calendar.JANUARY) {
                year--; 
            }
        } else if (month >= Calendar.FEBRUARY && month <= Calendar.APRIL) {
            season = "Second Crop Season";
        } else {
            season = "Off Season";
        }
        
        return new SeasonInfo(year, season);
    }

    private List<SeasonInfo> generateAllSeasons() {
        List<SeasonInfo> seasons = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        
        for (int year = currentYear; year > currentYear - YEARS_TO_SHOW; year--) {
            seasons.add(new SeasonInfo(year, "Main Crop Season"));
            seasons.add(new SeasonInfo(year, "Second Crop Season"));
            seasons.add(new SeasonInfo(year, "Off Season"));
        }
        
        return seasons;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detection_history);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        seasonSpinner = findViewById(R.id.seasonSpinner);
        seasonStats = findViewById(R.id.seasonStats);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DetectionHistoryAdapter(this);
        recyclerView.setAdapter(adapter);

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if (phoneNumber == null) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            phoneNumber = prefs.getString("phoneNumber", null);
        }

        if (phoneNumber == null) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Farmers")
                .child(phoneNumber)
                .child("detections");

        loadDetectionHistory();
    }

    private void loadDetectionHistory() {
        mDatabase.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<SeasonInfo, Map<String, Integer>> seasonalDetections = new HashMap<>();
                List<DetectionHistoryItem> detectionList = new ArrayList<>();
                
                List<SeasonInfo> availableSeasons = generateAllSeasons();
                
                for (SeasonInfo season : availableSeasons) {
                    seasonalDetections.put(season, new HashMap<>());
                }
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String classification = snapshot.child("classification").getValue(String.class);
                    Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                    String id = snapshot.getKey();

                    if (classification != null && timestamp != null) {
                        detectionList.add(new DetectionHistoryItem(classification, timestamp, id));
                        
                        SeasonInfo seasonInfo = getSeasonInfo(timestamp);
                        if (seasonalDetections.containsKey(seasonInfo)) {
                            Map<String, Integer> seasonCounts = seasonalDetections.get(seasonInfo);
                            seasonCounts.put(classification, seasonCounts.getOrDefault(classification, 0) + 1);
                        }
                    }
                }

                Collections.sort(availableSeasons, (s1, s2) -> {
                    int yearCompare = Integer.compare(s2.year, s1.year);
                    if (yearCompare != 0) return yearCompare;
                    
                    String[] seasonOrder = {"Main Crop Season", "Second Crop Season", "Off Season"};
                    return Integer.compare(
                        Arrays.asList(seasonOrder).indexOf(s1.season),
                        Arrays.asList(seasonOrder).indexOf(s2.season)
                    );
                });

                ArrayAdapter<SeasonInfo> spinnerAdapter = new ArrayAdapter<SeasonInfo>(
                    DetectionHistory.this,
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

                SeasonInfo currentSeason = getSeasonInfo(System.currentTimeMillis());
                int currentSeasonIndex = availableSeasons.indexOf(currentSeason);
                if (currentSeasonIndex != -1) {
                    seasonSpinner.setSelection(currentSeasonIndex);
                }

                seasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        SeasonInfo selectedSeason = (SeasonInfo) parent.getItemAtPosition(position);
                        Map<String, Integer> stats = seasonalDetections.get(selectedSeason);
                        
                        if (stats.isEmpty()) {
                            seasonStats.setText("No detections recorded for this season");
                        } else {
                            StringBuilder statsText = new StringBuilder();
                            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                                statsText.append(entry.getKey())
                                        .append(": ")
                                        .append(entry.getValue())
                                        .append("\n");
                            }
                            seasonStats.setText(statsText.toString());
                        }
                        
                        List<DetectionHistoryItem> filteredList = detectionList.stream()
                            .filter(item -> selectedSeason.equals(getSeasonInfo(item.getTimestamp())))
                            .collect(Collectors.toList());
                        
                        adapter.updateData(filteredList);
                        
                        if (filteredList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading detection history", databaseError.toException());
                Toast.makeText(DetectionHistory.this, 
                    "Error loading detection history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatSeasonDisplay(SeasonInfo info) {
        String seasonDisplay = info.season.replace(" Season", "");
        return info.year + " - " + seasonDisplay;
    }
}