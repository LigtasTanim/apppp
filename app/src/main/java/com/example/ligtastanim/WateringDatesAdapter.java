package com.example.ligtastanim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class WateringDatesAdapter extends RecyclerView.Adapter<WateringDatesAdapter.ViewHolder> {

    private Context context;
    private List<WateringDate> wateringDatesList;
    private DatabaseReference databaseReference;
    private final String uniqueIdentifier;
    private String phoneNumber;
    private String selectedFarmingActivity;

    public WateringDatesAdapter(Context context, List<WateringDate> wateringDatesList, DatabaseReference databaseReference, String uniqueIdentifier, String phoneNumber, String selectedFarmingActivity) {
        this.context = context;
        this.wateringDatesList = wateringDatesList;
        this.databaseReference = databaseReference;
        this.uniqueIdentifier = uniqueIdentifier;
        this.phoneNumber = phoneNumber;
        this.selectedFarmingActivity = selectedFarmingActivity;

        Log.d("WateringDatesAdapter", "selectedFarmingActivity: " + selectedFarmingActivity);
    }

    public void updateWateringDateStatus(int position) {
        if (selectedFarmingActivity == null || phoneNumber == null) {
            Toast.makeText(context, "Crop node or phone number is not set", Toast.LENGTH_SHORT).show();
            return;
        }

        WateringDate wateringDate = wateringDatesList.get(position);

        if ("ongoing".equals(wateringDate.status)) {
            String newStatus = "completed";
            wateringDate.status = newStatus;
            notifyItemChanged(position);

            DatabaseReference wateringDateRef = databaseReference.child(selectedFarmingActivity)
                    .child(uniqueIdentifier)
                    .child(phoneNumber)
                    .child("wateringDates")
                    .child(String.valueOf(position))
                    .child("status");

            wateringDateRef.setValue(newStatus)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Watering date status updated to completed", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update watering date status", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "Cannot update status. The current status is not 'ongoing'.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watering_date, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WateringDate wateringDate = wateringDatesList.get(position);
        holder.dateTextView.setText(wateringDate.date);
        holder.statusTextView.setText(wateringDate.status);

        // Show rescheduled date if it exists
        if (wateringDate.rescheduledDate != null) {
            holder.rescheduledDateTextView.setVisibility(View.VISIBLE);
            holder.rescheduledDateTextView.setText("Rescheduled to: " + wateringDate.rescheduledDate);
        } else {
            holder.rescheduledDateTextView.setVisibility(View.GONE);
        }

        // Set the status text color based on the status
        if ("completed".equals(wateringDate.status)) {
            holder.statusTextView.setTextColor(Color.GREEN);
        } else {
            holder.statusTextView.setTextColor(Color.RED);
        }

        holder.doneButton.setOnClickListener(v -> updateWateringDateStatus(position));
        
        holder.rescheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OngoingMonitoringActivity.class);
            intent.putExtra("isRescheduling", true);
            intent.putExtra("wateringDatePosition", position);
            intent.putExtra("currentDate", wateringDate.date);
            intent.putExtra("uniqueIdentifier", uniqueIdentifier);
            intent.putExtra("phoneNumber", phoneNumber);
            intent.putExtra("FarmingActivity", selectedFarmingActivity);
            ((Activity) context).startActivityForResult(intent, 1001);
        });
    }

    @Override
    public int getItemCount() {
        return wateringDatesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView statusTextView;
        TextView rescheduledDateTextView;
        Button doneButton;
        Button rescheduleButton;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.wateringDateTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            rescheduledDateTextView = itemView.findViewById(R.id.rescheduledDateTextView);
            doneButton = itemView.findViewById(R.id.statusUpdateButton);
            rescheduleButton = itemView.findViewById(R.id.rescheduleButton);
        }
    }

    public void updateRescheduledDate(int position, String newDate) {
        if (position >= 0 && position < wateringDatesList.size()) {
            WateringDate wateringDate = wateringDatesList.get(position);
            wateringDate.rescheduledDate = newDate;

            DatabaseReference dateRef = databaseReference.child(selectedFarmingActivity)
                    .child(uniqueIdentifier)
                    .child(phoneNumber)
                    .child("wateringDates")
                    .child(String.valueOf(position));

            Map<String, Object> updates = new HashMap<>();
            updates.put("rescheduledDate", newDate);

            dateRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        notifyItemChanged(position);
                        Toast.makeText(context, "Watering date rescheduled successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to reschedule watering date", Toast.LENGTH_SHORT).show());
        }
    }
}
