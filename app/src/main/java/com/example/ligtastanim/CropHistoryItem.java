package com.example.ligtastanim;

public class CropHistoryItem {
    private String selectedFarmingActivity;
    private String startDate;
    private double harvestingRentAmount;
    private double harvestingGasMoney;
    private double fertilizerCost;
    private double farmSize;
    private double wateringGasMoney;
    private double wateringRentAmount;
    private boolean isStatistics;
    private boolean isEmpty;


    public CropHistoryItem(String selectedFarmingActivity, String startDate, double harvestingRentAmount, double harvestingGasMoney, double fertilizerCost, double farmSize, double wateringGasMoney, double wateringRentAmount) {
        this.selectedFarmingActivity = selectedFarmingActivity;
        this.startDate = startDate;
        this.harvestingRentAmount = harvestingRentAmount;
        this.harvestingGasMoney = harvestingGasMoney;
        this.fertilizerCost = fertilizerCost;
        this.farmSize = farmSize;
        this.wateringGasMoney = wateringGasMoney;
        this.wateringRentAmount = wateringRentAmount;
    }

    public CropHistoryItem(String title) {
        this.selectedFarmingActivity = title;
    }

    public String getSelectedFarmingActivity() {
        return selectedFarmingActivity;
    }

    public String getStartDate() {
        return startDate;
    }

    public double getHarvestingRentAmount() {
        return harvestingRentAmount;
    }

    public double getHarvestingGasMoney() {
        return harvestingGasMoney;
    }

    public double getFertilizerCost() {
        return fertilizerCost;
    }

    public double getFarmSize() {
        return farmSize;
    }

    public double getWateringGasMoney() {
        return wateringGasMoney;
    }

    public double getWateringRentAmount() {
        return wateringRentAmount;
    }

    public boolean isStatistics() {
        return isStatistics;
    }

    public void setIsStatistics(boolean isStatistics) {
        this.isStatistics = isStatistics;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setIsEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }
}
