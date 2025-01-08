package com.example.ligtastanim;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class Notifications extends AppCompatActivity {

    private DatabaseReference notificationsRef;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        notificationsRef = FirebaseDatabase.getInstance().getReference().child("Announcement");

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();

        DatabaseReference farmerRef = FirebaseDatabase.getInstance().getReference()
            .child("Farmers")
            .child(phoneNumber);

        farmerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                
                adapter = new NotificationAdapter(Notifications.this, notificationList, 
                    phoneNumber, firstName, lastName);
                recyclerView.setAdapter(adapter);
                
                fetchAndDisplayNotifications();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Notifications.this,
                    "Failed to load farmer data", Toast.LENGTH_SHORT).show();
            }
        });

        markAnnouncementsAsSeen();

        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile();
            }
        });

        ImageView homeIcon = findViewById(R.id.homeIcon);
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });


        FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        String msg = "Subscribed to announcements";
                        if (!task.isSuccessful()) {
                            msg = "Subscription failed";
                        }
                        Log.d("FCM", msg);
                     }
        });
}

    private void fetchAndDisplayNotifications() {
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String body = snapshot.child("body").getValue(String.class);
                    Boolean archived = snapshot.child("archived").getValue(Boolean.class);
                    if (title != null && body != null && (archived == null || !archived)) {
                        Notification notification = new Notification(title, body);
                        notification.setId(snapshot.getKey());
                        notificationList.add(notification);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching notifications", databaseError.toException());
            }
        });
    }

    private void markAnnouncementsAsSeen() {
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!snapshot.hasChild("seen/" + phoneNumber)) {
                        snapshot.getRef().child("seen").child(phoneNumber).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goToProfile() {
        Intent intent = new Intent(Notifications.this, Profile.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

    private void goToHome() {
        Intent intent = new Intent(Notifications.this, Home.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }
}
