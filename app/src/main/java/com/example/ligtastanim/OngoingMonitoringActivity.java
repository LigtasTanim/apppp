package com.example.ligtastanim;

import static android.content.Intent.getIntent;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OngoingMonitoringActivity extends AppCompatActivity {

    private static final int CALENDAR_PERMISSION_CODE = 100;

    private TextView currentDateTextView, pestControlDateTextView, harvestDateTextView, pestStatusTextView, harvestStatusTextView;
    private RecyclerView wateringDatesRecyclerView;
    private WateringDatesAdapter wateringDatesAdapter;
    private List<WateringDate> wateringDatesList;

    private DatabaseReference databaseReference;
    private String phoneNumber;
    private String selectedFarmingActivity;
    private String uid;

    private CalendarView wateringScheduleCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_monitoring);

        requestCalendarPermissions();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        currentDateTextView = findViewById(R.id.currentDateTextView);
        pestControlDateTextView = findViewById(R.id.pestControlDateTextView);
        harvestDateTextView = findViewById(R.id.harvestDateTextView);
        wateringDatesRecyclerView = findViewById(R.id.wateringDatesRecyclerView);
        harvestStatusTextView = findViewById(R.id.harvestStatusTextView);
        pestStatusTextView = findViewById(R.id.pestStatusTextView);
        wateringDatesList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("CropsMonitoring");

        uid = getIntent().getStringExtra("uniqueIdentifier");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        selectedFarmingActivity = getIntent().getStringExtra("FarmingActivity");

        Log.d("OngoingMonitoringActivity", "UID: " + uid);
        Log.d("OngoingMonitoringActivity", "Phone Number: " + phoneNumber);
        Log.d("OngoingMonitoringActivity", "Farming Activity: " + selectedFarmingActivity);

        wateringDatesAdapter = new WateringDatesAdapter(this, wateringDatesList, databaseReference, uid, phoneNumber, selectedFarmingActivity);
        wateringDatesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        wateringDatesRecyclerView.setAdapter(wateringDatesAdapter);

        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(OngoingMonitoringActivity.this, v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_fill_monitoring, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_crops_history) {
                    Intent intent = new Intent(OngoingMonitoringActivity.this, CropsHistoryActivity.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    intent.putExtra("FarmingActivity", selectedFarmingActivity);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        Button completedButton = findViewById(R.id.completedButton);
        completedButton.setOnClickListener(v -> validateAndStartSummary());

        DatePicker newPestControlDatePicker = findViewById(R.id.newPestControlDatePicker);
        Button setNewPestControlDateButton = findViewById(R.id.setNewPestControlDateButton);

        setNewPestControlDateButton.setOnClickListener(v -> {
            int day = newPestControlDatePicker.getDayOfMonth();
            int month = newPestControlDatePicker.getMonth();
            int year = newPestControlDatePicker.getYear();

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            String newPestControlDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(calendar.getTime());

            updatePestControlDate(newPestControlDate);
        });

        wateringScheduleCalendar = findViewById(R.id.wateringScheduleCalendar);

        if (phoneNumber != null && selectedFarmingActivity != null && uid != null) {
            setCurrentDate();
            fetchAndDisplayData();
        } else {
            Toast.makeText(this, "Phone number or crop node not found", Toast.LENGTH_SHORT).show();
        }

        setupWateringScheduleCalendar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (phoneNumber != null && selectedFarmingActivity != null && uid != null) {
            fetchAndDisplayData();
        }
    }

    private void setCurrentDate() {
        String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        currentDateTextView.setText(currentDate);
    }

    private void fetchAndDisplayData() {
        databaseReference.child(selectedFarmingActivity).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(phoneNumber)) {
                    DataSnapshot phoneSnapshot = dataSnapshot.child(phoneNumber);

                    String startDateStr = phoneSnapshot.child("wateringDate").getValue(String.class);
                    String intervalDaysStr = phoneSnapshot.child("intervalDaysStr").getValue(String.class);

                    if (startDateStr != null && intervalDaysStr != null) {
                        try {
                            int intervalDays = Integer.parseInt(intervalDaysStr);
                            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(sdf.parse(startDateStr));

                            wateringDatesList.clear();
                            Map<String, Object> updates = new HashMap<>();

                            for (int i = 0; i < 6; i++) {
                                calendar.add(Calendar.DAY_OF_YEAR, intervalDays);
                                String wateringDate = sdf.format(calendar.getTime());
                                String status = phoneSnapshot.child("wateringDates").child(String.valueOf(i)).child("status").getValue(String.class);
                                status = status != null ? status : "ongoing";
                                wateringDatesList.add(new WateringDate(wateringDate, status));
                                updates.put("wateringDates/" + i + "/status", status);
                            }
                            wateringDatesAdapter.notifyDataSetChanged();

                            calendar.setTime(sdf.parse(startDateStr));
                            calendar.add(Calendar.DAY_OF_YEAR, 14);
                            String pestControlDate = sdf.format(calendar.getTime());
                            pestControlDateTextView.setText(pestControlDate);
                            updates.put("pestControlDate/status", "ongoing");

                            calendar.setTime(sdf.parse(startDateStr));
                            calendar.add(Calendar.DAY_OF_YEAR, 115);
                            String harvestDate = sdf.format(calendar.getTime());
                            harvestDateTextView.setText(harvestDate);
                            updates.put("harvestDate/status", "ongoing");

                            phoneSnapshot.getRef().updateChildren(updates);

                        } catch (Exception e) {
                            Toast.makeText(OngoingMonitoringActivity.this, "Error parsing dates", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        pestControlDateTextView.setText("No start date or interval found");
                        harvestDateTextView.setText("No start date or interval found");
                    }
                } else {
                    Toast.makeText(OngoingMonitoringActivity.this, "No data found for the given phone number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OngoingMonitoringActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndStartSummary() {
    }

    private void requestCalendarPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, CALENDAR_PERMISSION_CODE);
        }
    }

    private void updatePestControlDate(String newPestControlDate) {
        databaseReference.child(selectedFarmingActivity).child(uid)
            .child(phoneNumber).child("pestControlDate").setValue(newPestControlDate)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(OngoingMonitoringActivity.this, "Pest control date updated to: " + newPestControlDate, Toast.LENGTH_SHORT).show();
                    pestControlDateTextView.setText(newPestControlDate); // Update UI
                } else {
                    Toast.makeText(OngoingMonitoringActivity.this, "Failed to update pest control date", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setupWateringScheduleCalendar() {
        wateringScheduleCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", month + 1, dayOfMonth, year);
            for (WateringDate wateringDate : wateringDatesList) {
                if (wateringDate.getDate().equals(selectedDate)) {
                    Toast.makeText(this, "Watering Status: " + wateringDate.getStatus(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Toast.makeText(this, "No watering scheduled for this date", Toast.LENGTH_SHORT).show();
        });

        for (WateringDate wateringDate : wateringDatesList) {
        }
    }
}
