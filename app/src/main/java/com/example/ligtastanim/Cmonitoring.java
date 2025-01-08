package com.example.ligtastanim;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class Cmonitoring extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MonitoringAdapter monitoringAdapter;
    private List<MonitoringCrops> cropLists;
    private String phoneNumber;

    private static final String TAG = "Cmonitoring";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmonitoring);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cropLists = new ArrayList<>();
        cropLists.add(new MonitoringCrops("Rice", R.drawable.rice));
        cropLists.add(new MonitoringCrops("Corn", R.drawable.corn));
        cropLists.add(new MonitoringCrops("Ampalaya", R.drawable.ampalaya));

        monitoringAdapter = new MonitoringAdapter(this, cropLists, phoneNumber);
        recyclerView.setAdapter(monitoringAdapter);

        checkOngoingMonitoring();
    }

    public void checkOngoingMonitoring() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("CropsMonitoring");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean hasOngoingTask = false;
                String startDate = "";
                String cropType = "";
                Log.d(TAG, "Checking ongoing monitoring for phone number: " + phoneNumber);
                for (DataSnapshot cropSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "Checking crop type: " + cropSnapshot.getKey());
                    if (cropSnapshot.hasChild(phoneNumber)) {
                        Log.d(TAG, "Found phone number under crop type: " + cropSnapshot.getKey());
                        DataSnapshot userSnapshot = cropSnapshot.child(phoneNumber);
                        String status = userSnapshot.child("MainStatus").getValue(String.class);
                        Log.d(TAG, "MainStatus: " + status);
                        if ("ongoing".equals(status)) {
                            hasOngoingTask = true;
                            startDate = userSnapshot.child("startDate").getValue(String.class);
                            cropType = cropSnapshot.getKey();
                            Log.d(TAG, "Start Date: " + startDate);
                            break;
                        }
                    } else {
                        Log.d(TAG, "Phone number not found under crop type: " + cropSnapshot.getKey());
                    }
                }
                if (hasOngoingTask) {
                    Log.d(TAG, "Navigating to OngoingMonitoringActivity with startDate: " + startDate + " and cropType: " + cropType);
                    Intent intent = new Intent(Cmonitoring.this, OngoingMonitoringActivity.class);
                    intent.putExtra("startDate", startDate);
                    intent.putExtra("phoneNumber", phoneNumber);
                    intent.putExtra("cropType", cropType);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d(TAG, "No ongoing task found.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }
}
