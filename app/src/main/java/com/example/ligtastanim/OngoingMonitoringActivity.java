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
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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
import java.text.ParseException;
import java.util.Date;

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
    private GridView calendarGrid;
    private CustomCalendarAdapter calendarAdapter;

    private ImageButton previousMonth, nextMonth;
    private TextView monthYearTV;
    private TextView newPendingDateTextView;

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
        newPendingDateTextView = findViewById(R.id.newPendingDateTextView);
        wateringDatesList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("CropsMonitoring");

        uid = getIntent().getStringExtra("uniqueIdentifier");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        selectedFarmingActivity = getIntent().getStringExtra("FarmingActivity");
        boolean isRescheduling = getIntent().getBooleanExtra("isRescheduling", false);
        int wateringDatePosition = getIntent().getIntExtra("wateringDatePosition", -1);
        String currentDate = getIntent().getStringExtra("currentDate");

        Log.d("OngoingMonitoringActivity", "UID: " + uid);
        Log.d("OngoingMonitoringActivity", "Phone Number: " + phoneNumber);
        Log.d("OngoingMonitoringActivity", "Farming Activity: " + selectedFarmingActivity);
        Log.d("OngoingMonitoringActivity", "Is Rescheduling: " + isRescheduling);

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

            newPendingDateTextView.setVisibility(View.VISIBLE);
            newPendingDateTextView.setText("New Date (Pending): " + newPestControlDate);

            updatePestControlDate(newPestControlDate);
        });

        calendarGrid = findViewById(R.id.calendarGrid);

        Button showDatePickerButton = findViewById(R.id.showDatePickerButton);
        LinearLayout datePickerContainer = findViewById(R.id.datePickerContainer);

        showDatePickerButton.setOnClickListener(v -> {
            // Toggle visibility
            if (datePickerContainer.getVisibility() == View.VISIBLE) {
                datePickerContainer.setVisibility(View.GONE);
                showDatePickerButton.setText("Change Pest Control Date");
            } else {
                datePickerContainer.setVisibility(View.VISIBLE);
                showDatePickerButton.setText("Hide Date Picker");
            }
        });

        Button statusUpdatePest = findViewById(R.id.statusUpdatePest);
        Button statusUpdateHarvest = findViewById(R.id.statusUpdateHarvest);

        // Click listener for Pest Control status update
        statusUpdatePest.setOnClickListener(v -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("pestControlDate/status", "completed");

            databaseReference.child(selectedFarmingActivity).child(uid)
                .child(phoneNumber)
                .updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pestStatusTextView.setText("completed");
                        pestStatusTextView.setTextColor(Color.GREEN);
                        Toast.makeText(OngoingMonitoringActivity.this, 
                            "Pest Control status updated to completed", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OngoingMonitoringActivity.this, 
                            "Failed to update status", 
                            Toast.LENGTH_SHORT).show();
                    }
                });
        });

        // Click listener for Harvest status update
        statusUpdateHarvest.setOnClickListener(v -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("harvestDate/status", "completed");

            databaseReference.child(selectedFarmingActivity).child(uid)
                .child(phoneNumber)
                .updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        harvestStatusTextView.setText("completed");
                        harvestStatusTextView.setTextColor(Color.GREEN);
                        Toast.makeText(OngoingMonitoringActivity.this, 
                            "Harvest status updated to completed", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OngoingMonitoringActivity.this, 
                            "Failed to update status", 
                            Toast.LENGTH_SHORT).show();
                    }
                });
        });

        if (phoneNumber != null && selectedFarmingActivity != null && uid != null) {
            setCurrentDate();
            fetchAndDisplayData();
        } else {
            Toast.makeText(this, "Phone number or crop node not found", Toast.LENGTH_SHORT).show();
        }

        setupCalendar();

        // If we're rescheduling, update the UI to reflect that
        if (isRescheduling) {
            TextView calendarLabel = findViewById(R.id.calendarLabel);
            calendarLabel.setText("Select New Watering Date");
            
            // Modify calendar click listener for rescheduling
            calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
                if (position < 7) return;
                
                position -= 7;
                Calendar cal = (Calendar) calendarAdapter.getCalendar().clone();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                int firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK) - 1;
                cal.add(Calendar.DAY_OF_MONTH, position - firstDayOfMonth);

                String selectedDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(cal.getTime());
                
                // Update the watering date
                Intent resultIntent = new Intent();
                resultIntent.putExtra("newDate", selectedDate);
                resultIntent.putExtra("position", wateringDatePosition);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }
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

                            DataSnapshot pestControlSnapshot = phoneSnapshot.child("pestControlDate");
                            String originalSchedule = pestControlSnapshot.child("originalSchedule").getValue(String.class);
                            String reschedule = pestControlSnapshot.child("reschedule").getValue(String.class);
                            String pestStatus = pestControlSnapshot.child("status").getValue(String.class);
                            
                            // Update pest control status
                            if (pestStatus != null) {
                                pestStatusTextView.setText(pestStatus);
                                // Set color based on status
                                if (pestStatus.equals("completed")) {
                                    pestStatusTextView.setTextColor(Color.GREEN);
                                } else {
                                    pestStatusTextView.setTextColor(Color.RED);
                                }
                            } else {
                                pestStatusTextView.setText("ongoing");
                                pestStatusTextView.setTextColor(Color.RED);
                            }

                            if (originalSchedule != null) {
                                pestControlDateTextView.setText(originalSchedule);
                            }
                            
                            if (reschedule != null) {
                                newPendingDateTextView.setVisibility(View.VISIBLE);
                                newPendingDateTextView.setText("Rescheduled to: " + reschedule);
                            } else {
                                newPendingDateTextView.setVisibility(View.GONE);
                            }

                            // Get and update harvest status
                            String harvestStatus = phoneSnapshot.child("harvestDate").child("status").getValue(String.class);
                            if (harvestStatus != null) {
                                harvestStatusTextView.setText(harvestStatus);
                                // Set color based on status
                                if (harvestStatus.equals("completed")) {
                                    harvestStatusTextView.setTextColor(Color.GREEN);
                                } else {
                                    harvestStatusTextView.setTextColor(Color.RED);
                                }
                            } else {
                                harvestStatusTextView.setText("ongoing");
                                harvestStatusTextView.setTextColor(Color.RED);
                            }

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
        // Create a map to update multiple values
        Map<String, Object> updates = new HashMap<>();
        updates.put("pestControlDate/originalSchedule", pestControlDateTextView.getText().toString());
        updates.put("pestControlDate/reschedule", newPestControlDate);
        updates.put("pestControlDate/status", "rescheduled");

        databaseReference.child(selectedFarmingActivity).child(uid)
            .child(phoneNumber)
            .updateChildren(updates)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(OngoingMonitoringActivity.this, "Pest control date rescheduled", Toast.LENGTH_SHORT).show();
                    newPendingDateTextView.setVisibility(View.VISIBLE);
                    newPendingDateTextView.setText("Rescheduled to: " + newPestControlDate);
                } else {
                    Toast.makeText(OngoingMonitoringActivity.this, "Failed to reschedule pest control date", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setupCalendar() {
        calendarGrid = findViewById(R.id.calendarGrid);
        previousMonth = findViewById(R.id.previousMonth);
        nextMonth = findViewById(R.id.nextMonth);
        monthYearTV = findViewById(R.id.monthYearTV);

        calendarAdapter = new CustomCalendarAdapter(this, wateringDatesList);
        calendarGrid.setAdapter(calendarAdapter);
        
        // Update month year display
        updateMonthYear();

        // Setup month navigation
        previousMonth.setOnClickListener(v -> {
            calendarAdapter.previousMonth();
            updateMonthYear();
        });

        nextMonth.setOnClickListener(v -> {
            calendarAdapter.nextMonth();
            updateMonthYear();
        });

        calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
            // Skip if clicking on week day labels
            if (position < 7) return;

            // Adjust position to account for week day labels
            position -= 7;

            Calendar cal = (Calendar) calendarAdapter.getCalendar().clone();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK) - 1;
            cal.add(Calendar.DAY_OF_MONTH, position - firstDayOfMonth);

            String selectedDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(cal.getTime());
            
            // Find if this date is already a watering date
            int wateringDateIndex = -1;
            for (int i = 0; i < wateringDatesList.size(); i++) {
                if (wateringDatesList.get(i).getDate().equals(selectedDate)) {
                    wateringDateIndex = i;
                    break;
                }
            }

            // If it's an existing watering date
            if (wateringDateIndex != -1) {
                WateringDate wateringDate = wateringDatesList.get(wateringDateIndex);
                updateWateringDateInDatabase(wateringDateIndex, selectedDate, wateringDate.getStatus());
            } else {
                // Add new watering date
                addNewWateringDate(selectedDate);
            }
        });
    }

    private void updateMonthYear() {
        monthYearTV.setText(calendarAdapter.getMonthYear());
    }

    private void updateWateringDateInDatabase(int index, String date, String currentStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("wateringDates/" + index + "/date", date);
        updates.put("wateringDates/" + index + "/status", "ongoing");

        databaseReference.child(selectedFarmingActivity)
            .child(uid)
            .child(phoneNumber)
            .updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Watering date updated", Toast.LENGTH_SHORT).show();
                wateringDatesList.get(index).date = date;
                wateringDatesAdapter.notifyDataSetChanged();
                calendarAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to update watering date", Toast.LENGTH_SHORT).show()
            );
    }

    private void addNewWateringDate(String date) {
        int newIndex = wateringDatesList.size();
        if (newIndex >= 6) {
            Toast.makeText(this, "Maximum number of watering dates reached", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("wateringDates/" + newIndex + "/date", date);
        updates.put("wateringDates/" + newIndex + "/status", "ongoing");

        databaseReference.child(selectedFarmingActivity)
            .child(uid)
            .child(phoneNumber)
            .updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                wateringDatesList.add(new WateringDate(date, "ongoing"));
                wateringDatesAdapter.notifyDataSetChanged();
                calendarAdapter.notifyDataSetChanged();
                Toast.makeText(this, "New watering date added", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to add watering date", Toast.LENGTH_SHORT).show()
            );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String newDate = data.getStringExtra("newDate");
            int position = data.getIntExtra("position", -1);
            if (position != -1 && wateringDatesAdapter != null) {
                wateringDatesAdapter.updateRescheduledDate(position, newDate);
            }
        }
    }
}
