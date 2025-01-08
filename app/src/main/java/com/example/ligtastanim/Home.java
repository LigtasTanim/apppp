package com.example.ligtastanim;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Home extends AppCompatActivity {

    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private String phoneNumber;
    private View redDot;
    private DatabaseReference mDatabase;
    private List<UpcomingEvent> upcomingEvents;
    private UpcomingEventAdapter upcomingEventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView getNotification = findViewById(R.id.notification_bell);
        getNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Notifications.class);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Intent intents = getIntent();
        phoneNumber = intents.getStringExtra("phoneNumber");

        // Add this call to check farmer status
        checkFarmerStatus();

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        categoryList = new ArrayList<>();
        categoryList.add(new Category("Crops", R.drawable.ic_crops));
        categoryList.add(new Category("Pests", R.drawable.ic_pest));
        categoryList.add(new Category("Fertilizers", R.drawable.ic_fertilizer));
        categoryList.add(new Category("Insecticide", R.drawable.ic_insecticide));
        categoryList.add(new Category("Disease", R.drawable.ic_disease));

        categoryAdapter = new CategoryAdapter(categoryList, this);
        categoriesRecyclerView.setAdapter(categoryAdapter);


        RelativeLayout cropsMonitoring = findViewById(R.id.cropsMonitoring);
        cropsMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCropsMonitoring();
            }
        });

        TextView history = findViewById(R.id.textViewHistory);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHistory();
            }
        });

        LinearLayout join_Association = findViewById(R.id.joinAssociation);
        join_Association.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, JoinAssociation.class);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);
            }
        });

        redDot = findViewById(R.id.red_dot);

        checkForNewAnnouncements();

        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile();
            }
        });

        ImageView notification = findViewById(R.id.notification_bell);
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNotifications();
            }
        });

        TextView consult = findViewById(R.id.consultDaText);
        consult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { goToConsult(); }
        });

        RelativeLayout pestDetection = findViewById(R.id.pestDetection);
        pestDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Retrieve the Bitmap from the camera intent
            Bitmap image = (Bitmap) data.getExtras().get("data");

            // Compress the Bitmap to a byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            // Pass the byte array to the second activity
            Intent intent = new Intent(Home.this, Detection.class);

            intent.putExtra("capturedImage", byteArray);
            intent.putExtra("phoneNumber", phoneNumber);

            startActivity(intent);
        }
    }

    private void checkFarmerStatus() {
        LinearLayout joinAssociation = findViewById(R.id.joinAssociation);

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            mDatabase.child("Farmers").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String status = dataSnapshot.child("status").getValue(String.class);
                        String association = dataSnapshot.child("association").getValue(String.class);

                        if ("Approved".equals(status) && "not a member of any association".equals(association)) {
                            joinAssociation.setVisibility(View.VISIBLE);
                        } else {
                            joinAssociation.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Home", "Error checking farmer status", databaseError.toException());
                }
            });
        }
    }


    private void checkForNewAnnouncements() {
        DatabaseReference announcementsRef = FirebaseDatabase.getInstance().getReference("Announcement");

        announcementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean hasNew = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!snapshot.hasChild("seen/" + phoneNumber)) {
                        hasNew = true;
                        break;
                    }
                }
                if (hasNew) {
                    redDot.setVisibility(View.VISIBLE);
                } else {
                    redDot.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkFarmerStatus();
        checkForNewAnnouncements();
    }

    private void goToProfile() {
        Intent intent = new Intent(Home.this, Profile.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

    private void goToHistory() {
        Intent intent = new Intent(Home.this, DetectionHistory.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

    private void goToConsult() {
        Intent intent = new Intent(Home.this, ChatList.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

    private void goToNotifications() {
        Intent intent = new Intent(Home.this, Notifications.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

    private void goToCropsMonitoring() {
        Intent intent = new Intent(Home.this, FillMonitoring.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

    private void goToPestDetection() {
        Intent intent = new Intent(Home.this, SelectCropToDetect.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

    private void checkProfileCompletion() {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            mDatabase.child("Farmers").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String address = dataSnapshot.child("address").getValue(String.class);
                        String dateOfBirth = dataSnapshot.child("dateOfBirth").getValue(String.class);
                        String placeOfBirth = dataSnapshot.child("placeOfBirth").getValue(String.class);
                        String religion = dataSnapshot.child("religion").getValue(String.class);
                        String civilStatus = dataSnapshot.child("civilStatus").getValue(String.class);
                        String farmingActivity = dataSnapshot.child("farmingActivity").getValue(String.class);
                        if (address == null || address.isEmpty() ||
                            dateOfBirth == null || dateOfBirth.isEmpty() ||
                        placeOfBirth == null || placeOfBirth.isEmpty() ||
                        religion == null || religion.isEmpty() ||
                        civilStatus == null || civilStatus.isEmpty() ||
                        farmingActivity == null || farmingActivity.isEmpty()) {
                            showCompleteProfileDialog();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Home", "Error checking profile completion", databaseError.toException());
                }
            });
        }
    }

    private void showCompleteProfileDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Complete Your Profile")
            .setMessage("Please complete your profile information to fully access all features of the app.")
            .setPositiveButton("Complete Now", (dialog, which) -> {
                Intent intent = new Intent(Home.this, EditProfileActivity.class);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);
            })
            .setNegativeButton("Later", (dialog, which) -> dialog.dismiss())
            .setCancelable(false)
            .show();
    }


}
