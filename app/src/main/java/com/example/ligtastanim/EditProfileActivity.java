package com.example.ligtastanim;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextMiddleName, editTextLastName, editTextExtensionName,
            editTextAddress, editTextDateOfBirth, editTextPlaceOfBirth, editTextReligion,
            editTextCivilStatus;
    private CheckBox checkBoxAmpalaya, checkBoxPalay, checkBoxMais;
    private Button buttonSave, buttonSelectImage;
    private ImageView profileImage;
    private Uri imageUri;
    private DatabaseReference mDatabase;
    private String phoneNumber;
    private static final String TAG = "EditProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_edit_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        if (phoneNumber == null) {
            Log.e(TAG, "Phone number is null");
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextMiddleName = findViewById(R.id.editTextMiddleName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextExtensionName = findViewById(R.id.editTextExtensionName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth);
        editTextPlaceOfBirth = findViewById(R.id.editTextPlaceOfBirth);
        editTextReligion = findViewById(R.id.editTextReligion);
        editTextCivilStatus = findViewById(R.id.editTextCivilStatus);
        checkBoxAmpalaya = findViewById(R.id.checkBoxAmpalaya);
        checkBoxPalay = findViewById(R.id.checkBoxPalay);
        checkBoxMais = findViewById(R.id.checkBoxMais);
        buttonSave = findViewById(R.id.buttonSave);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        profileImage = findViewById(R.id.profileImage);

        fetchCurrentUser();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateProfile();
            }
        });

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        editTextDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                uploadImageToFirebaseStorage(imageUri);
            } catch (IOException e) {
                Log.e(TAG, "Error getting image: " + e.getMessage(), e);
            }
        }
    }

    private void fetchCurrentUser() {
        if (phoneNumber != null) {
            mDatabase.child("Farmers").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String middleName = dataSnapshot.child("middleName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);
                        String extensionName = dataSnapshot.child("extensionName").getValue(String.class);
                        String address = dataSnapshot.child("address").getValue(String.class);
                        String dateOfBirth = dataSnapshot.child("dateOfBirth").getValue(String.class);
                        String placeOfBirth = dataSnapshot.child("placeOfBirth").getValue(String.class);
                        String religion = dataSnapshot.child("religion").getValue(String.class);
                        String civilStatus = dataSnapshot.child("civilStatus").getValue(String.class);
                        String farmingActivity = dataSnapshot.child("farmingActivity").getValue(String.class);

                        editTextFirstName.setText(firstName);
                        editTextMiddleName.setText(middleName);
                        editTextLastName.setText(lastName);
                        editTextExtensionName.setText(extensionName);
                        editTextAddress.setText(address);
                        editTextDateOfBirth.setText(dateOfBirth);
                        editTextPlaceOfBirth.setText(placeOfBirth);
                        editTextReligion.setText(religion);
                        editTextCivilStatus.setText(civilStatus);

                        if (farmingActivity != null) {
                            String[] activities = farmingActivity.split(", ");
                            for (String activity : activities) {
                                switch (activity.trim()) {
                                    case "Ampalaya ":
                                        checkBoxAmpalaya.setChecked(true);
                                        break;
                                    case "Palay ":
                                        checkBoxPalay.setChecked(true);
                                        break;
                                    case "Mais ":
                                        checkBoxMais.setChecked(true);
                                        break;
                                }
                            }
                        }

                        String imageUrl = dataSnapshot.child("profileImage").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            loadProfileImage(imageUrl);
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
        }
    }

    private void updateProfile() {
        String firstName = editTextFirstName.getText().toString();
        String middleName = editTextMiddleName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String extensionName = editTextExtensionName.getText().toString();
        String address = editTextAddress.getText().toString();
        String dateOfBirth = editTextDateOfBirth.getText().toString();
        String placeOfBirth = editTextPlaceOfBirth.getText().toString();
        String religion = editTextReligion.getText().toString();
        String civilStatus = editTextCivilStatus.getText().toString();
        StringBuilder farmingActivities = new StringBuilder();
        if (checkBoxAmpalaya.isChecked()) farmingActivities.append("Ampalaya,");
        if (checkBoxPalay.isChecked()) farmingActivities.append("Palay,");
        if (checkBoxMais.isChecked()) farmingActivities.append("Mais,");
        
        String farmingActivity = farmingActivities.length() > 0 ? 
            farmingActivities.substring(0, farmingActivities.length() - 1) : "";

        if (phoneNumber != null) {
            DatabaseReference userRef = mDatabase.child("Farmers").child(phoneNumber);

            // Update fields in the "Farmers" table
            userRef.child("firstName").setValue(firstName);
            userRef.child("middleName").setValue(middleName);
            userRef.child("lastName").setValue(lastName);
            userRef.child("extensionName").setValue(extensionName);
            userRef.child("address").setValue(address);
            userRef.child("dateOfBirth").setValue(dateOfBirth);
            userRef.child("placeOfBirth").setValue(placeOfBirth);
            userRef.child("religion").setValue(religion);
            userRef.child("civilStatus").setValue(civilStatus);
            userRef.child("farmingActivity").setValue(farmingActivity);

            // Fetch the current association to update the "association" table
            userRef.child("association").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String association = dataSnapshot.getValue(String.class);
                        if (association != null && !association.isEmpty()) {
                            DatabaseReference associationRef = mDatabase.child("association").child(association).child(phoneNumber);

                            // Update fields in the "association" table
                            associationRef.child("firstName").setValue(firstName);
                            associationRef.child("middleName").setValue(middleName);
                            associationRef.child("lastName").setValue(lastName);
                            associationRef.child("extensionName").setValue(extensionName);
                            associationRef.child("address").setValue(address);
                            associationRef.child("dateOfBirth").setValue(dateOfBirth);
                            associationRef.child("placeOfBirth").setValue(placeOfBirth);
                            associationRef.child("religion").setValue(religion);
                            associationRef.child("civilStatus").setValue(civilStatus);
                            associationRef.child("farmingActivity").setValue(farmingActivity);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to fetch association: " + databaseError.getMessage());
                }
            });

            Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Phone number is null");
            Toast.makeText(EditProfileActivity.this, "Failed to update profile. Phone number is null.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference profileImageRef = storageRef.child("profile_images/" + phoneNumber + ".jpg");

            profileImageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            mDatabase.child("Farmers").child(phoneNumber).child("profileImage").setValue(downloadUrl);
                            loadProfileImage(downloadUrl);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failed to upload image: " + e.getMessage());
                }
            });
        }
    }

    private void loadProfileImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .transform(new CircleCrop())
                .into(profileImage);
    }

    private void showDatePickerDialog() {
        // Get Current Date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, monthOfYear, dayOfMonth);
                        
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        editTextDateOfBirth.setText(dateFormat.format(selectedDate.getTime()));
                    }
                }, year, month, day);
        
        // Optional: Set max date to current date
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        
        datePickerDialog.show();
    }
}
