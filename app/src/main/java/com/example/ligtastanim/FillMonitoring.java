package com.example.ligtastanim;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class  FillMonitoring extends AppCompatActivity {

    private EditText wateringDateEditText, intervalDaysEditText, edtStartDate;
    private TextView tv4, tv5;
    private Spinner spinnerFarmingActivity;
    private RadioGroup rgWateringEquipment, rgHarvestingEquipment;
    private Button btnStartMonitoring, btnContinue;
    private DatabaseReference databaseReference;
    private String phoneNumber;
    private TextView tvFertilizerRecommendation;
    private String selectedFarmingActivity;
    private String uniqueIdentifier;
    private SimpleDateFormat dateFormatter;
    private Spinner spinnerVariety;
    private HashMap<String, String> fertilizerRecommendations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_monitoring);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("CropsMonitoring");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        Log.d("FillMonitoring", "Phone number received: " + phoneNumber);

        edtStartDate = findViewById(R.id.startDate);
        spinnerFarmingActivity = findViewById(R.id.spinnerFarmingActivity);

        wateringDateEditText = findViewById(R.id.wateringDate);
        intervalDaysEditText = findViewById(R.id.intervalDays);
        btnStartMonitoring = findViewById(R.id.btnStartMonitoring);
        btnContinue = findViewById(R.id.continuebtn);
        tv4 = findViewById(R.id.tv4);
        tv5 = findViewById(R.id.tv5);
        spinnerVariety = findViewById(R.id.spinnerVariety);
        
        tvFertilizerRecommendation = findViewById(R.id.tvFertilizerRecommendation);

        initializeFertilizerData();

        btnStartMonitoring.setOnClickListener(v -> checkOngoingMonitoring());

        btnContinue.setOnClickListener(v -> checkMainStatus());

        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(FillMonitoring.this, v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_fill_monitoring, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_crops_history) {
                    Intent intent = new Intent(FillMonitoring.this, CropsHistoryActivity.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    intent.putExtra("FarmingActivity", selectedFarmingActivity);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });


        wateringDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isValidDate(s.toString())) {
                    wateringDateEditText.setError("Invalid date format. Use MM-DD-YYYY");
                }
            }
        });

        edtStartDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isValidDate(s.toString())) {
                    edtStartDate.setError("Invalid date format. Use MM-DD-YYYY");
                }
            }
        });

        dateFormatter = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        
        edtStartDate.setOnClickListener(v -> showDatePicker(edtStartDate));
        wateringDateEditText.setOnClickListener(v -> showDatePicker(wateringDateEditText));

        spinnerFarmingActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFarmingActivity = parent.getItemAtPosition(position).toString();
                if (!selectedFarmingActivity.isEmpty() && !selectedFarmingActivity.equals("Choose Your Crop")) {
                    tvFertilizerRecommendation.setVisibility(View.VISIBLE);
                    String recommendation = fertilizerRecommendations.get(selectedFarmingActivity);
                    tvFertilizerRecommendation.setText(recommendation != null ? recommendation : "No recommendation available.");
                    
                    populateVarieties(selectedFarmingActivity);
                } else {
                    tvFertilizerRecommendation.setVisibility(View.GONE);
                    spinnerVariety.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvFertilizerRecommendation.setVisibility(View.GONE);
                spinnerVariety.setVisibility(View.GONE);
            }
        });

        spinnerVariety.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedVariety = parent.getItemAtPosition(position).toString();
                if (!selectedVariety.equals("Choose Your Variety")) {
                    tvFertilizerRecommendation.setVisibility(View.VISIBLE);
                    String recommendation = fertilizerRecommendations.get(selectedVariety);
                    tvFertilizerRecommendation.setText(recommendation != null ? recommendation : "No recommendation available.");
                } else {
                    tvFertilizerRecommendation.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvFertilizerRecommendation.setVisibility(View.GONE);
            }
        });
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void checkOngoingMonitoring() {
        String selectedFarmingActivity = spinnerFarmingActivity.getSelectedItem().toString();

        databaseReference.child(selectedFarmingActivity).orderByChild(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean hasOngoing = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MonitoringData data = snapshot.child(phoneNumber).getValue(MonitoringData.class);
                    if (data != null && "ongoing".equals(data.MainStatus)) {
                        hasOngoing = true;
                        break;
                    }
                }

                if (hasOngoing) {
                    Toast.makeText(FillMonitoring.this, "Cannot start a new monitoring session while an ongoing one exists.", Toast.LENGTH_SHORT).show();
                } else {
                    startMonitoring();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FillMonitoring.this, "Failed to check ongoing monitoring.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkMainStatus() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot farmingActivitySnapshot : dataSnapshot.getChildren()) {
                    String farmingActivity = farmingActivitySnapshot.getKey();  // Get farming activity
                    for (DataSnapshot uniqueIdentifierSnapshot : farmingActivitySnapshot.getChildren()) {
                        DataSnapshot phoneSnapshot = uniqueIdentifierSnapshot.child(phoneNumber);
                        if (phoneSnapshot.exists()) {
                            String mainStatus = phoneSnapshot.child("MainStatus").getValue(String.class);
                            String uid = uniqueIdentifierSnapshot.getKey();  // Get UID

                            if ("ongoing".equals(mainStatus)) {
                                Intent intent = new Intent(FillMonitoring.this, OngoingMonitoringActivity.class);
                                intent.putExtra("phoneNumber", phoneNumber);
                                intent.putExtra("FarmingActivity", farmingActivity);
                                intent.putExtra("uniqueIdentifier", uid);
                                startActivity(intent);
                                return;
                            }
                        }
                    }
                }
                Toast.makeText(FillMonitoring.this, "No current ongoing monitoring", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FillMonitoring.this, "Failed to check main status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeFertilizerData() {
        fertilizerRecommendations = new HashMap<>();
        // Rice varieties
        fertilizerRecommendations.put("Mestiso 19", "Recommended Fertilizer:40kg /ha Nitrogen, 14-14-14");
        fertilizerRecommendations.put("Mestiso 20", "Recommended Fertilizer:40kg /ha Nitrogen, 14-14-14");
        fertilizerRecommendations.put("Mestiso 38", "Recommended Fertilizer:40kg /ha Nitrogen, 14-14-14");
        fertilizerRecommendations.put("Mestiso 47", "Recommended Fertilizer:40kg /ha Nitrogen, 14-14-14");
        fertilizerRecommendations.put("Mestiso 55", "Recommended Fertilizer:40kg /ha Nitrogen, 14-14-14");

        // Corn varieties
        fertilizerRecommendations.put("Sweet Fortune F1", "Recommended Fertilizer:40kg /ha Nitrogen, 14-14-14");
        fertilizerRecommendations.put("Jolly Fiesta F1", "Recommended Fertilizer:40kg /ha Nitrogen, 14-14-14");
        fertilizerRecommendations.put("Sweet Delight F1", "Recommended Fertilizer:40kg /ha Nitrogen, 14-14-14");
        fertilizerRecommendations.put("Ruby Queen F1", "Recommended Fertilizer:40kg /ha Nitrogen, 14-14-14");
        fertilizerRecommendations.put("Vinkle F1", "Recommended Fertilizer:60kg /ha Nitrogen");
        fertilizerRecommendations.put("Honey Baby", "Recommended Fertilizer:50kg /ha Nitrogen");
        fertilizerRecommendations.put("Honey King", "Recommended Fertilizer:0kg /ha Nitrogen");

        // Ampalaya varieties
        fertilizerRecommendations.put("Verde Buenas F1", "Recommended Fertilizer:14-14-14");
        fertilizerRecommendations.put("Verde Swerte F1", "Recommended Fertilizer:14-14-14");
        fertilizerRecommendations.put("Sta. Isabelle", "Recommended Fertilizer:14-14-14");
        fertilizerRecommendations.put("Ilokano Green", "Recommended Fertilizer:14-14-14");
    }

    private void startMonitoring() {
        String startDate = edtStartDate.getText().toString().trim();
        String selectedFarmingActivity = spinnerFarmingActivity.getSelectedItem().toString();
        String wateringDate = wateringDateEditText.getText().toString().trim();
        String intervalDaysStr = intervalDaysEditText.getText().toString().trim();

        if (TextUtils.isEmpty(selectedFarmingActivity) || TextUtils.isEmpty(startDate)) {
            Toast.makeText(this, "Please fill all the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String uniqueIdentifier = databaseReference.push().getKey();

        MonitoringData monitoringData = new MonitoringData(
                startDate,
                wateringDate,
                intervalDaysStr,
                "ongoing"
        );

        if (uniqueIdentifier != null) {
            databaseReference.child(selectedFarmingActivity).child(uniqueIdentifier).child(phoneNumber).setValue(monitoringData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FillMonitoring", "Monitoring data saved successfully.");
                            Intent intent = new Intent(FillMonitoring.this, OngoingMonitoringActivity.class);
                            intent.putExtra("phoneNumber", phoneNumber);  // Ensure consistency in key name
                            intent.putExtra("FarmingActivity", selectedFarmingActivity);
                            intent.putExtra("uniqueIdentifier", uniqueIdentifier);  // Pass the UID here
                            Toast.makeText(FillMonitoring.this, "Monitoring started successfully", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        } else {
                            Log.e("FillMonitoring", "Failed to save monitoring data.");
                            Toast.makeText(FillMonitoring.this, "Failed to start monitoring", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("FillMonitoring", "Unique Identifier is null.");
        }
    }

    private void showDatePicker(EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, month, dayOfMonth);
                dateField.setText(dateFormatter.format(newDate.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void populateVarieties(String farmingActivity) {
        ArrayAdapter<String> varietyAdapter;
        switch (farmingActivity) {
            case "Rice":
                varietyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                        new String[]{"Choose Your Variety", "Mestiso 19", "Mestiso 20", "Mestiso 38", "Mestiso 47", "Mestiso 55"});
                break;
            case "Corn":
                varietyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                        new String[]{"Choose Your Variety", "Sweet Fortune F1", "Jolly Fiesta F1", "Sweet Delight F1", "Ruby Queen F1", "Vinkle F1", "Honey Baby", "Honey King"});
                break;
            case "Ampalaya":
                varietyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                        new String[]{"Choose Your Variety", "Verde Buenas F1", "Verde Swerte F1", "Sta. Isabelle", "Ilokano Green"});
                break;
            default:
                varietyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Choose Your Variety"});
                break;
        }

        varietyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVariety.setAdapter(varietyAdapter);
        spinnerVariety.setVisibility(View.VISIBLE);
    }

    static class MonitoringData {
        public String startDate;
        public String wateringDate;
        public String intervalDaysStr;
        public String MainStatus;

        public MonitoringData() {
        }

        public MonitoringData(String startDate, String wateringDate, 
                            String intervalDaysStr, String MainStatus) {
            this.startDate = startDate;
            this.wateringDate = wateringDate;
            this.intervalDaysStr = intervalDaysStr;
            this.MainStatus = MainStatus;
        }
    }
}
