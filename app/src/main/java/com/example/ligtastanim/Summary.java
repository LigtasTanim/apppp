package com.example.ligtastanim;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Summary extends AppCompatActivity {

    private EditText wateringGasMoneyEditText, wateringRentAmountEditText;
    private EditText harvestingRentAmountEditText, harvestingGasMoneyEditText;
    private EditText fertilizerCostEditText;
    private TextView totalExpensesTextView, expectedSackHarvestTextView;
    private RadioGroup wateringEquipmentRadioGroup, harvestingEquipmentRadioGroup;
    private Button completedButton;
    private DatabaseReference databaseReference;
    private String phoneNumber, selectedFarmingActivity, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupRadioGroupListeners();

        Intent intent = getIntent();
        if (intent != null) {
            uid = intent.getStringExtra("uniqueIdentifier");
            phoneNumber = intent.getStringExtra("phoneNumber");
            selectedFarmingActivity = intent.getStringExtra("FarmingActivity");

            Log.d("Summary", "UID: " + uid);
            Log.d("Summary", "Phone Number: " + phoneNumber);
            Log.d("Summary", "Farming Activity: " + selectedFarmingActivity);

            if (uid == null || phoneNumber == null || selectedFarmingActivity == null) {
                Toast.makeText(this, "Missing required data from previous screen", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(this, "No data received from previous screen", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("CropsMonitoring");

        if (phoneNumber != null) {
            fetchAndDisplayExpenses();
        }

        completedButton.setOnClickListener(v -> saveDataAndUpdateStatus());
    }

    private void initializeViews() {
        wateringGasMoneyEditText = findViewById(R.id.wateringGasMoneyEditText);
        wateringRentAmountEditText = findViewById(R.id.wateringRentAmountEditText);
        harvestingRentAmountEditText = findViewById(R.id.harvestingRentAmountEditText);
        harvestingGasMoneyEditText = findViewById(R.id.harvestingGasMoneyEditText);
        fertilizerCostEditText = findViewById(R.id.fertilizerCostEditText);
        totalExpensesTextView = findViewById(R.id.totalExpensesTextView);
        expectedSackHarvestTextView = findViewById(R.id.expectedHarvestTextView);
        wateringEquipmentRadioGroup = findViewById(R.id.wateringEquipmentRadioGroup);
        harvestingEquipmentRadioGroup = findViewById(R.id.harvestingEquipmentRadioGroup);
        completedButton = findViewById(R.id.completedButton);
    }

    private void setupRadioGroupListeners() {
        wateringEquipmentRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.wateringYesRadio) {
                wateringGasMoneyEditText.setVisibility(View.VISIBLE);
                wateringRentAmountEditText.setVisibility(View.GONE);
            } else if (checkedId == R.id.wateringNoRadio) {
                wateringGasMoneyEditText.setVisibility(View.GONE);
                wateringRentAmountEditText.setVisibility(View.VISIBLE);
            }
            updateTotalExpenses();
        });

        harvestingEquipmentRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.harvestingYesRadio) {
                harvestingGasMoneyEditText.setVisibility(View.VISIBLE);
                harvestingRentAmountEditText.setVisibility(View.GONE);
            } else if (checkedId == R.id.harvestingNoRadio) {
                harvestingGasMoneyEditText.setVisibility(View.GONE);
                harvestingRentAmountEditText.setVisibility(View.VISIBLE);
            }
            updateTotalExpenses();
        });
    }

    private void fetchAndDisplayExpenses() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot farmingActivitySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot uniqueIdentifierSnapshot : farmingActivitySnapshot.getChildren()) {
                        if (uniqueIdentifierSnapshot.hasChild(phoneNumber)) {
                            DataSnapshot phoneSnapshot = uniqueIdentifierSnapshot.child(phoneNumber);
                            
                            wateringGasMoneyEditText.setText(phoneSnapshot.child("wateringGasMoney").getValue(String.class));
                            wateringRentAmountEditText.setText(phoneSnapshot.child("wateringRentAmount").getValue(String.class));
                            harvestingGasMoneyEditText.setText(phoneSnapshot.child("harvestingGasMoney").getValue(String.class));
                            harvestingRentAmountEditText.setText(phoneSnapshot.child("harvestingRentAmount").getValue(String.class));
                            fertilizerCostEditText.setText(phoneSnapshot.child("fertilizerCost").getValue(String.class));

                            double farmSize = getDoubleValue(phoneSnapshot.child("farmSize").getValue(String.class));
                            double expectedSackHarvest = farmSize * 129;
                            expectedSackHarvestTextView.setText(String.format("Expected Sack Harvest: %.2f sacks", expectedSackHarvest));

                            updateTotalExpenses();
                            return;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Summary.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalExpenses() {
        double wateringGas = getDoubleFromEditText(wateringGasMoneyEditText) * 6;
        double wateringRent = getDoubleFromEditText(wateringRentAmountEditText) * 6;
        double harvestingGas = getDoubleFromEditText(harvestingGasMoneyEditText);
        double harvestingRent = getDoubleFromEditText(harvestingRentAmountEditText);
        double fertilizerCost = getDoubleFromEditText(fertilizerCostEditText);

        double totalExpenses = wateringGas + wateringRent + harvestingRent + harvestingGas + fertilizerCost;
        totalExpensesTextView.setText(String.format("Total Expenses: %.2f", totalExpenses));
    }

    private double getDoubleFromEditText(EditText editText) {
        try {
            String value = editText.getText().toString();
            return value.isEmpty() ? 0.0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void saveDataAndUpdateStatus() {
        if (selectedFarmingActivity == null || uid == null || phoneNumber == null) {
            String errorMessage = "Missing required data: ";
            if (selectedFarmingActivity == null) errorMessage += "Farming Activity, ";
            if (uid == null) errorMessage += "UID, ";
            if (phoneNumber == null) errorMessage += "Phone Number, ";
            errorMessage = errorMessage.substring(0, errorMessage.length() - 2);
            
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e("Summary", errorMessage);
            return;
        }

        try {
            DatabaseReference userRef = databaseReference.child(selectedFarmingActivity)
                    .child(uid).child(phoneNumber);

            Map<String, Object> updates = new HashMap<>();
            updates.put("wateringGasMoney", wateringGasMoneyEditText.getText().toString());
            updates.put("wateringRentAmount", wateringRentAmountEditText.getText().toString());
            updates.put("harvestingGasMoney", harvestingGasMoneyEditText.getText().toString());
            updates.put("harvestingRentAmount", harvestingRentAmountEditText.getText().toString());
            updates.put("fertilizerCost", fertilizerCostEditText.getText().toString());
            updates.put("MainStatus", "completed");

            userRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Summary.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Summary.this, Home.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("FarmingActivity", selectedFarmingActivity);
                        intent.putExtra("uniqueIdentifier", uid);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Summary.this, "Failed to save data", Toast.LENGTH_SHORT).show();
                        Log.e("Summary", "Error saving data", e);
                    });
        } catch (Exception e) {
            String errorMessage = "Error creating database reference: " + e.getMessage();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e("Summary", errorMessage, e);
        }
    }

    private double getDoubleValue(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}