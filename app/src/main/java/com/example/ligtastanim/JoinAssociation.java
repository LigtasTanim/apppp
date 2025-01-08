package com.example.ligtastanim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinAssociation extends AppCompatActivity {
    private static final int IMAGE_PICK_REQUEST = 1;
    private String currentImageType;
    private String phoneNumber;
    private ImageView imageView2x2, imageViewLandTitle, imageViewLandTax, imageViewCertification, imageViewValidID, imageViewReceipt;
    private Uri uri2x2, uriLandTitle, uriLandTax, uriCertification, uriValidID, uriReceipt;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private Button btnSubmit;
    private Spinner spinnerAssociation;
    private List<String> associationList = new ArrayList<>();
    private static final String TAG = "JoinAssociation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_association);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Phone number is required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        imageView2x2 = findViewById(R.id.imageView2x2);
        imageViewLandTitle = findViewById(R.id.imageViewLandTitle);
        imageViewLandTax = findViewById(R.id.imageViewLandTax);
        imageViewCertification = findViewById(R.id.imageViewCertification);
        imageViewValidID = findViewById(R.id.imageViewValidID);
        imageViewReceipt = findViewById(R.id.imageViewReceipt);
        btnSubmit = findViewById(R.id.btnSubmit);
        spinnerAssociation = findViewById(R.id.spinnerAssociation);

        setupImageClickListeners();
        setupSubmitButton();
        loadAssociations();
    }

    private void loadAssociations() {
        mDatabase.child("association").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                associationList.clear(); 
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String associationName = snapshot.getKey();
                    if (associationName != null) {
                        associationList.add(associationName);
                    }
                }
                
                java.util.Collections.sort(associationList);
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    JoinAssociation.this,
                    android.R.layout.simple_spinner_item,
                    associationList
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAssociation.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to load associations", databaseError.toException());
                Toast.makeText(JoinAssociation.this, "Failed to load associations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            if (validateAllImagesSelected() && validateAssociationSelected()) {
                uploadAllImages();
            } else {
                Toast.makeText(this, "Please select all required images and an association", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateAllImagesSelected() {
        return uri2x2 != null && uriLandTitle != null && uriLandTax != null && 
               uriCertification != null && uriValidID != null && uriReceipt != null;
    }

    private boolean validateAssociationSelected() {
        return spinnerAssociation.getSelectedItem() != null;
    }

    private void uploadAllImages() {
        uploadImage(uri2x2, "2x2");
        uploadImage(uriLandTitle, "LandTitle");
        uploadImage(uriLandTax, "LandTax");
        uploadImage(uriCertification, "Certification");
        uploadImage(uriValidID, "ValidID");
        uploadImage(uriReceipt, "Receipt");
        
        String selectedAssociation = spinnerAssociation.getSelectedItem().toString();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Pending");
        updates.put("selectedAssociation", selectedAssociation);
        
        mDatabase.child("Farmers").child(phoneNumber)
            .updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to submit application", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to submit application", e);
            });
    }

    private void setupImageClickListeners() {
        imageView2x2.setOnClickListener(v -> openImagePicker("2x2"));
        imageViewLandTitle.setOnClickListener(v -> openImagePicker("LandTitle"));
        imageViewLandTax.setOnClickListener(v -> openImagePicker("LandTax"));
        imageViewCertification.setOnClickListener(v -> openImagePicker("Certification"));
        imageViewValidID.setOnClickListener(v -> openImagePicker("ValidID"));
        imageViewReceipt.setOnClickListener(v -> openImagePicker("Receipt"));
    }

    private void openImagePicker(String imageType) {
        currentImageType = imageType;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            switch (currentImageType) {
                case "2x2":
                    uri2x2 = selectedImageUri;
                    imageView2x2.setImageURI(selectedImageUri);
                    break;
                case "LandTitle":
                    uriLandTitle = selectedImageUri;
                    imageViewLandTitle.setImageURI(selectedImageUri);
                    break;
                case "LandTax":
                    uriLandTax = selectedImageUri;
                    imageViewLandTax.setImageURI(selectedImageUri);
                    break;
                case "Certification":
                    uriCertification = selectedImageUri;
                    imageViewCertification.setImageURI(selectedImageUri);
                    break;
                case "ValidID":
                    uriValidID = selectedImageUri;
                    imageViewValidID.setImageURI(selectedImageUri);
                    break;
                case "Receipt":
                    uriReceipt = selectedImageUri;
                    imageViewReceipt.setImageURI(selectedImageUri);
                    break;
            }
        }
    }

    private void uploadImage(Uri imageUri, String imageType) {
        if (imageUri != null) {
            StorageReference imageRef = mStorage.child("Farmers").child(phoneNumber).child(imageType + ".jpg");

            imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> saveImageUriToDatabase(uri.toString(), imageType));
            }).addOnFailureListener(e -> {
                Toast.makeText(JoinAssociation.this, "Failed to upload " + imageType + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to upload " + imageType, e);
            });
        } else {
            Log.d(TAG, "No image selected for " + imageType);
        }
    }

    private void saveImageUriToDatabase(String imageUrl, String imageType) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            DatabaseReference farmerRef = mDatabase.child("Farmers").child(phoneNumber).child("Requirements");
            farmerRef.child(imageType).setValue(imageUrl).addOnSuccessListener(aVoid -> {
                Log.d(TAG, imageType + " URL saved to database successfully.");
                Toast.makeText(JoinAssociation.this, imageType + " uploaded successfully", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save " + imageType + " URL to database", e);
                Toast.makeText(JoinAssociation.this, "Failed to save " + imageType + " to database", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Phone number is required before uploading images", Toast.LENGTH_SHORT).show();
        }
    }
}