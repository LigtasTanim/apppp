package com.example.ligtastanim;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CropsHistoryAdapter extends RecyclerView.Adapter<CropsHistoryAdapter.ViewHolder> {

    private List<CropHistoryItem> cropHistoryList;
    private Context context;

    public CropsHistoryAdapter(Context context, List<CropHistoryItem> cropHistoryList) {
        this.context = context;
        this.cropHistoryList = cropHistoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crops_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CropHistoryItem item = cropHistoryList.get(position);

        String farmingActivity = item.getSelectedFarmingActivity();
        String startDate = item.getStartDate();


        double harvestingRentAmount = item.getHarvestingRentAmount();
        double harvestingGasMoney = item.getHarvestingGasMoney();
        double fertilizerCost = item.getFertilizerCost();
        double farmSize = item.getFarmSize();
        double wateringGasMoney = item.getWateringGasMoney();
        double wateringRentAmount = item.getWateringRentAmount();

        holder.cropTextView.setText(farmingActivity != null && !farmingActivity.isEmpty() ? farmingActivity : "No activity data");
        holder.dateTextView.setText(startDate != null && !startDate.isEmpty() ? startDate : "No start date");

        Log.d("CropsHistoryAdapter", "Item at position " + position + ": " + farmingActivity + ", " + startDate);


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CropsHistoryDetails.class);
            intent.putExtra("FarmingActivity", farmingActivity);
            intent.putExtra("StartDate", startDate);
            intent.putExtra("HarvestingRentAmount", String.valueOf(item.getHarvestingRentAmount()));
            intent.putExtra("HarvestingGasMoney", String.valueOf(item.getHarvestingGasMoney()));
            intent.putExtra("FertilizerCost", String.valueOf(item.getFertilizerCost()));
            intent.putExtra("FarmSize", String.valueOf(item.getFarmSize()));
            intent.putExtra("WateringGasMoney", String.valueOf(item.getWateringGasMoney()));
            intent.putExtra("WateringRentAmount", String.valueOf(item.getWateringRentAmount()));
            context.startActivity(intent);
        });

    }


        @Override
    public int getItemCount() {
        return cropHistoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cropTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cropTextView = itemView.findViewById(R.id.cropTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
