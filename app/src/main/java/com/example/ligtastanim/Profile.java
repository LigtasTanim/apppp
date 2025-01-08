package com.example.ligtastanim;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private TextView textViewFirstName, textViewAssociation, textViewLastName, textViewMiddleName, textViewExtensionName, textViewDateOfBirth, textViewPlaceOfBirth, textViewSex, textViewIsPWD, textViewReligion, textViewCivilStatus, textViewFarmingActivity, textViewAddress, textViewPhoneNumber;
    private ImageView profileImage;
    private Button editProfileButton, buttonLogout;
    private ImageButton dropdownButton;
    private LinearLayout dropdownMenu;
    private TextView viewOfficers, joinAssociation, transferAssociation;
    private TextView textViewApprovedAssociation;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        String phoneNumber = intent.getStringExtra("phoneNumber");

        textViewFirstName = findViewById(R.id.textViewFirstName);
        textViewLastName = findViewById(R.id.textViewLastName);
        textViewMiddleName = findViewById(R.id.textViewMiddleName);
        textViewExtensionName = findViewById(R.id.textViewExtensionName);
        textViewDateOfBirth = findViewById(R.id.textViewDateOfBirth);
        textViewPlaceOfBirth = findViewById(R.id.textViewPlaceOfBirth);
        textViewSex = findViewById(R.id.textViewSex);
        textViewReligion = findViewById(R.id.textViewReligion);
        textViewCivilStatus = findViewById(R.id.textViewCivilStatus);
        textViewFarmingActivity = findViewById(R.id.textViewFarmingActivity);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);
        textViewAssociation = findViewById(R.id.textViewAssociation);
        profileImage = findViewById(R.id.profileImage);
        editProfileButton = findViewById(R.id.editProfileButton);
        buttonLogout = findViewById(R.id.buttonLogout);
        dropdownButton = findViewById(R.id.dropdownButton);
        dropdownMenu = findViewById(R.id.dropdownMenu);
        viewOfficers = findViewById(R.id.viewOfficers);
        joinAssociation = findViewById(R.id.joinAssociation);
        transferAssociation = findViewById(R.id.transferAssociation);
        textViewApprovedAssociation = findViewById(R.id.textViewApprovedAssociation);

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, EditProfileActivity.class);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        dropdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dropdownMenu.getVisibility() == View.VISIBLE) {
                    dropdownMenu.setVisibility(View.GONE);
                } else {
                    dropdownMenu.setVisibility(View.VISIBLE);
                }
            }
        });

        viewOfficers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String association = textViewAssociation.getText().toString();
                if (!association.isEmpty()) {
                    Intent intent = new Intent(Profile.this, ViewOfficersActivity.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    intent.putExtra("association", association);
                    startActivity(intent);
                } else {
                    Toast.makeText(Profile.this, "No association found", Toast.LENGTH_SHORT).show();
                }
                dropdownMenu.setVisibility(View.GONE);
            }
        });

        joinAssociation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, JoinAssociation.class);
                intent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"));
                startActivity(intent);
                dropdownMenu.setVisibility(View.GONE);
            }
        });

        transferAssociation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, TransferAssociationActivity.class);
                intent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"));
                intent.putExtra("currentAssociation", textViewAssociation.getText().toString());
                startActivity(intent);
                dropdownMenu.setVisibility(View.GONE);
            }
        });

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            fetchCurrentUser(phoneNumber);
        } else {
            Log.e(TAG, "Phone number is null or empty");
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Profile.this, Login.class));
            finish();
        }
    }

    private void fetchCurrentUser(String phoneNumber) {
        if (phoneNumber != null) {
            mDatabase.child("Farmers").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Register.User user = dataSnapshot.getValue(Register.User.class);
                        if (user != null) {

                            textViewFirstName.setText(user.firstName != null ? user.firstName : "N/A");
                            textViewLastName.setText(user.lastName != null ? user.lastName : "N/A");
                            textViewMiddleName.setText(user.middleName != null ? user.middleName : "N/A");
                            textViewExtensionName.setText(user.extensionName != null ? user.extensionName : "N/A");
                            textViewDateOfBirth.setText(user.dateOfBirth != null ? user.dateOfBirth : "");
                            textViewAssociation.setText(user.association != null ? user.association : "");
                            textViewPlaceOfBirth.setText(user.placeOfBirth != null ? user.placeOfBirth : "");
                            textViewSex.setText(user.sex != null ? user.sex : "N/A");
                            textViewReligion.setText(user.religion != null ? user.religion : "");
                            textViewCivilStatus.setText(user.civilStatus != null ? user.civilStatus : "");
                            textViewFarmingActivity.setText(user.farmingActivity != null ? user.farmingActivity : "");
                            textViewAddress.setText(user.address != null ? user.address : "N/A");
                            textViewPhoneNumber.setText(user.phoneNumber != null ? user.phoneNumber : "N/A");


                            String imageUrl = dataSnapshot.child("profileImage").getValue(String.class);
                            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                                loadProfileImage(imageUrl);
                            }

                            String approvedAssociation = user.approvedAssociation;
                            if (approvedAssociation != null && !approvedAssociation.isEmpty()) {
                                textViewApprovedAssociation.setText(approvedAssociation);
                                textViewApprovedAssociation.setVisibility(View.VISIBLE);
                            } else {
                                textViewApprovedAssociation.setVisibility(View.GONE);
                            }
                        } else {
                            Log.e(TAG, "User data is null");
                        }
                    } else {
                        Log.e(TAG, "User data does not exist for phone number: " + phoneNumber);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to fetch user data: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e(TAG, "Phone number is null");
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Profile.this, Login.class));
            finish();
        }
    }

    private void loadProfileImage(String imageUrl) {
        try {
            Glide.with(Profile.this)
                    .load(imageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(profileImage);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load profile image: " + e.getMessage());
        }
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(Profile.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(Profile.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
