package com.example.ligtastanim;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CropsHistoryDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crops_history_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        TextView farmingActivityTextView = findViewById(R.id.farmingActivityTextView);
        TextView startDateTextView = findViewById(R.id.startDateTextView);
        TextView harvestingRentAmountTextView = findViewById(R.id.harvestingRentAmountTextView);
        TextView harvestingGasMoneyTextView = findViewById(R.id.harvestingGasMoneyTextView);
        TextView fertilizerCostTextView = findViewById(R.id.fertilizerCostTextView);
        TextView farmSizeTextView = findViewById(R.id.farmSizeTextView);
        TextView wateringGasMoneyTextView = findViewById(R.id.wateringGasMoneyTextView);
        TextView wateringRentAmountTextView = findViewById(R.id.wateringRentAmountTextView);
        TextView totalExpensesTextView = findViewById(R.id.totalExpensesTextView);


        String farmingActivity = getIntent().getStringExtra("FarmingActivity");
        String startDate = getIntent().getStringExtra("StartDate");
        String harvestingRentAmountStr = getIntent().getStringExtra("HarvestingRentAmount");
        String harvestingGasMoneyStr = getIntent().getStringExtra("HarvestingGasMoney");
        String fertilizerCostStr = getIntent().getStringExtra("FertilizerCost");
        String farmSizeStr = getIntent().getStringExtra("FarmSize");
        String wateringGasMoneyStr = getIntent().getStringExtra("WateringGasMoney");
        String wateringRentAmountStr = getIntent().getStringExtra("WateringRentAmount");


        double harvestingRentAmount = harvestingRentAmountStr != null && !harvestingRentAmountStr.isEmpty() ? Double.parseDouble(harvestingRentAmountStr) : 0.0;
        double harvestingGasMoney = harvestingGasMoneyStr != null && !harvestingGasMoneyStr.isEmpty() ? Double.parseDouble(harvestingGasMoneyStr) : 0.0;
        double fertilizerCost = fertilizerCostStr != null && !fertilizerCostStr.isEmpty() ? Double.parseDouble(fertilizerCostStr) : 0.0;
        double farmSize = farmSizeStr != null && !farmSizeStr.isEmpty() ? Double.parseDouble(farmSizeStr) : 0.0;
        double wateringGasMoney = wateringGasMoneyStr != null && !wateringGasMoneyStr.isEmpty() ? Double.parseDouble(wateringGasMoneyStr) : 0.0;
        double wateringRentAmount = wateringRentAmountStr != null && !wateringRentAmountStr.isEmpty() ? Double.parseDouble(wateringRentAmountStr) : 0.0;


        Log.d("CropsHistoryDetails", "HarvestingRentAmount: " + harvestingRentAmount);
        Log.d("CropsHistoryDetails", "HarvestingGasMoney: " + harvestingGasMoney);
        Log.d("CropsHistoryDetails", "FertilizerCost: " + fertilizerCost);
        Log.d("CropsHistoryDetails", "FarmSize: " + farmSize);
        Log.d("CropsHistoryDetails", "WateringGasMoney: " + wateringGasMoney);
        Log.d("CropsHistoryDetails", "WateringRentAmount: " + wateringRentAmount);


        if (farmingActivity != null) {
            farmingActivityTextView.setText(farmingActivity);
        }
        if (startDate != null) {
            startDateTextView.setText(startDate);
        }
        wateringGasMoneyTextView.setText(String.format("Watering Gas Money: %.2f", wateringGasMoney * 6));
        wateringRentAmountTextView.setText(String.format("Watering Rent Amount: %.2f", wateringRentAmount * 6));
        harvestingRentAmountTextView.setText(String.format("Harvesting Rent Amount: %.2f", harvestingRentAmount));
        harvestingGasMoneyTextView.setText(String.format("Harvesting Gas Money: %.2f", harvestingGasMoney));
        fertilizerCostTextView.setText(String.format("Fertilizer Cost: %.2f", fertilizerCost));
        farmSizeTextView.setText(String.format("Farm Size: %.2f", farmSize));


        double totalExpenses = (wateringGasMoney * 6) + (wateringRentAmount * 6) + harvestingRentAmount + harvestingGasMoney + fertilizerCost;


        if (totalExpensesTextView != null) {
            totalExpensesTextView.setText(String.format("Total Expenses: %.2f", totalExpenses));
        }
    }
}
