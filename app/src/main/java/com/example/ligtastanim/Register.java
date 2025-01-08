package com.example.ligtastanim;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Register extends AppCompatActivity {

    private EditText editTextFirstName, editTextMiddleName, editTextLastName, editTextExtensionName, editTextAddress, editTextPhoneNumber, editTextPassword;

    private RadioGroup radioGroupSex;
    private Button buttonSendOTP, buttonNext, buttonBack, buttonNext2, buttonBack2;
    private boolean isPasswordVisible = false;
    private DatabaseReference mDatabase;
    private EditText editTextConfirmPassword;
    private TextView textViewConfirmPassword;
    private TextView textViewBarangay, textViewMunicipality, textViewProvince, textViewAddress, textViewAssociation, textViewPhoneNumber, textViewPassword, textViewFirstName, textViewMiddleName, textViewLastName, textViewExtensionName, textViewSex, textView2x2, textViewLandTitle, textViewLandTax, textViewCertification, textViewValitID;
    private static final int IMAGE_PICK_REQUEST = 1001;
    private Uri uri2x2, uriLandTitle, uriLandTax, uriCertification, uriValidID, selectedImageUri;
    private String currentImageType;
    private StorageReference mStorage;
    private static final String TAG = "RegisterActivity";
    private Spinner spinnerProvince, spinnerMunicipality, spinnerBarangay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        textViewConfirmPassword = findViewById(R.id.textViewConfirmPassword);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextMiddleName = findViewById(R.id.editTextMiddleName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextExtensionName = findViewById(R.id.editTextExtensionName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSendOTP = findViewById(R.id.buttonSendOTP);
        radioGroupSex = findViewById(R.id.radioGroupSex);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewAssociation = findViewById(R.id.textViewAssociation);
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);
        textViewPassword = findViewById(R.id.textViewPassword);
        buttonNext = findViewById(R.id.buttonNext);

        textViewFirstName = findViewById(R.id.textViewFirstName);
        textViewMiddleName = findViewById(R.id.textViewMiddleName);
        textViewLastName = findViewById(R.id.textViewLastName);
        textViewExtensionName = findViewById(R.id.textViewExtensionName);
        textViewSex = findViewById(R.id.textViewSex);
        buttonBack = findViewById(R.id.buttonBack);

        textViewBarangay = findViewById(R.id.textViewBarangay);
        textViewMunicipality = findViewById(R.id.textViewMunicipality);
        textViewProvince = findViewById(R.id.textViewProvince);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerMunicipality = findViewById(R.id.spinnerMunicipality);
        spinnerBarangay = findViewById(R.id.spinnerBarangay);

        setupSpinners();
        textViewBarangay.setVisibility(GONE);
        textViewMunicipality.setVisibility(GONE);
        textViewProvince.setVisibility(GONE);
        buttonBack.setVisibility(GONE);
        spinnerProvince.setVisibility(View.GONE);
        spinnerMunicipality.setVisibility(View.GONE);
        spinnerBarangay.setVisibility(View.GONE);
        editTextPhoneNumber.setVisibility(GONE);
        editTextPassword.setVisibility(GONE);
        textViewPhoneNumber.setVisibility(GONE);
        textViewPassword.setVisibility(GONE);
        buttonSendOTP.setVisibility(GONE);
        editTextConfirmPassword.setVisibility(GONE);
        textViewConfirmPassword.setVisibility(GONE);


        editTextPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editTextPassword.getRight() - editTextPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        togglePasswordVisibility();
                        return true;
                    }
                }
                return false;
            }
        });

        buttonSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOTP();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewBarangay.setVisibility(View.VISIBLE);
                textViewMunicipality.setVisibility(View.VISIBLE);
                textViewProvince.setVisibility(View.VISIBLE);
                editTextPhoneNumber.setVisibility(View.VISIBLE);
                editTextPassword.setVisibility(View.VISIBLE);
                textViewPhoneNumber.setVisibility(View.VISIBLE);
                textViewPassword.setVisibility(View.VISIBLE);
                buttonBack.setVisibility(View.VISIBLE);
                editTextConfirmPassword.setVisibility(View.VISIBLE);
                textViewConfirmPassword.setVisibility(View.VISIBLE);

                buttonSendOTP.setVisibility(VISIBLE);
                editTextFirstName.setVisibility(View.GONE);
                editTextMiddleName.setVisibility(View.GONE);
                editTextLastName.setVisibility(View.GONE);
                editTextExtensionName.setVisibility(View.GONE);
                radioGroupSex.setVisibility(View.GONE);
                textViewFirstName.setVisibility(View.GONE);
                textViewMiddleName.setVisibility(View.GONE);
                textViewLastName.setVisibility(View.GONE);
                textViewExtensionName.setVisibility(View.GONE);
                textViewSex.setVisibility(View.GONE);
                buttonNext.setVisibility(View.GONE);

                spinnerProvince.setVisibility(View.VISIBLE);
                spinnerMunicipality.setVisibility(View.VISIBLE);
                spinnerBarangay.setVisibility(View.VISIBLE);

                if (validateFirstPage()) {
            }
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewBarangay.setVisibility(GONE);
                textViewMunicipality.setVisibility(GONE);
                textViewProvince.setVisibility(GONE);
                editTextPhoneNumber.setVisibility(View.GONE);
                editTextPassword.setVisibility(View.GONE);
                textViewPhoneNumber.setVisibility(View.GONE);
                textViewPassword.setVisibility(View.GONE);
                buttonSendOTP.setVisibility(GONE);
                buttonBack.setVisibility(View.GONE);
                editTextConfirmPassword.setVisibility(GONE);
                textViewConfirmPassword.setVisibility(GONE);

                editTextFirstName.setVisibility(View.VISIBLE);
                editTextMiddleName.setVisibility(View.VISIBLE);
                editTextLastName.setVisibility(View.VISIBLE);
                editTextExtensionName.setVisibility(View.VISIBLE);
                radioGroupSex.setVisibility(View.VISIBLE);
                textViewFirstName.setVisibility(View.VISIBLE);
                textViewMiddleName.setVisibility(View.VISIBLE);
                textViewLastName.setVisibility(View.VISIBLE);
                textViewExtensionName.setVisibility(View.VISIBLE);
                textViewSex.setVisibility(View.VISIBLE);
                buttonNext.setVisibility(View.VISIBLE);

                spinnerProvince.setVisibility(View.GONE);
                spinnerMunicipality.setVisibility(View.GONE);
                spinnerBarangay.setVisibility(View.GONE);
            }
        });
    }

    private boolean validateFirstPage() {
    String firstName = editTextFirstName.getText().toString().trim();
    String lastName = editTextLastName.getText().toString().trim();
    int selectedSexId = radioGroupSex.getCheckedRadioButtonId();

    if (firstName.isEmpty()) {
        editTextFirstName.setError("First name is required");
        return false;
    }
    if (lastName.isEmpty()) {
        editTextLastName.setError("Last name is required");
        return false;
    }
    if (selectedSexId == -1) {
        Toast.makeText(this, "Please select your sex", Toast.LENGTH_SHORT).show();
        return false;
    }
    return true;
}

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
        } else {
            editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0);
        }
        isPasswordVisible = !isPasswordVisible;
        editTextPassword.setSelection(editTextPassword.getText().length());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> provinceAdapter = ArrayAdapter.createFromResource(this,
                R.array.provinces, android.R.layout.simple_spinner_item);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        ArrayAdapter<CharSequence> municipalityAdapter = ArrayAdapter.createFromResource(this,
                R.array.municipalities, android.R.layout.simple_spinner_item);
        municipalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMunicipality.setAdapter(municipalityAdapter);

        ArrayAdapter<CharSequence> barangayAdapter = ArrayAdapter.createFromResource(this,
                R.array.barangays, android.R.layout.simple_spinner_item);
        barangayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBarangay.setAdapter(barangayAdapter);
    }

    private void sendOTP() {
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String firstName = editTextFirstName.getText().toString().trim();
        String middleName = editTextMiddleName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String extensionName = editTextExtensionName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        
        String province = spinnerProvince.getSelectedItem().toString();
        String municipality = spinnerMunicipality.getSelectedItem().toString();
        String barangay = spinnerBarangay.getSelectedItem().toString();
        
        if (province.equals("Select Province") || 
            municipality.equals("Select Municipality") || 
            barangay.equals("Select Barangay")) {
            Toast.makeText(this, "Please select complete address", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String completeAddress = barangay + ", " + municipality + ", " + province;

        int selectedSexId = radioGroupSex.getCheckedRadioButtonId();
        String sex = selectedSexId != -1 ? ((RadioButton) findViewById(selectedSexId)).getText().toString() : "";
        String association = "not a member of any association";
        String dateOfBirth = "";
        String placeOfBirth = "";
        String religion = "";
        String civilStatus = "";
        String farmingActivity = "";

        if (firstName.isEmpty() || lastName.isEmpty() || 
            phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Required fields are empty");
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Passwords do not match");
            return;
        }

        if (!phoneNumber.startsWith("+63")) {
            Toast.makeText(this, "Phone number must start with +63", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Phone number does not start with +63");
            return;
        }

        if (phoneNumber.length() != 13) {
            Toast.makeText(this, "Phone number must be 13 digits long", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Phone number is not 13 digits long");
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Password is less than 8 characters long");
            return;
        }

        mDatabase.child("Farmers").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(Register.this, "Phone number is already used", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Phone number is already used: " + phoneNumber);
                } else {
                    String hashedPassword = hashPassword(password);
                    Log.d(TAG, "Hashed Password: " + hashedPassword);

                    User user = new User(firstName, middleName, lastName, extensionName, 
                        dateOfBirth, placeOfBirth, sex, completeAddress, phoneNumber, 
                        association, hashedPassword, religion, civilStatus, farmingActivity);

                    mDatabase.child("Farmers").child(phoneNumber).setValue(user)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                saveFcmToken(phoneNumber);
                                Intent intent = new Intent(Register.this, OTPpage.class);
                                intent.putExtra("phone", phoneNumber);
                                startActivity(intent);
                                Log.d(TAG, "User registered successfully" + phoneNumber);
                            } else {
                                Toast.makeText(Register.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Registration failed", task.getException());
                            }
                        });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Register.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error: ", databaseError.toException());
            }
        });
    }

    private void saveFcmToken(String phoneNumber) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    if (token != null) {
                        mDatabase.child("Farmers").child(phoneNumber).child("fcmToken").setValue(token)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved successfully for " + phoneNumber))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to save FCM token", e));
                    }
                });
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Hashing algorithm not found", e);
            return null;
        }
    }

    static class User {
        public String firstName;
        public String middleName;
        public String lastName;
        public String extensionName;
        public String dateOfBirth;
        public String placeOfBirth;
        public String sex;

        public String address;
        public String phoneNumber;
        public String association;
        public String password;
        public String religion;
        public String civilStatus;
        public String farmingActivity;
        public String approvedAssociation;

        public User() {
        }

        public User(String firstName, String middleName, String lastName, String extensionName, String dateOfBirth,
                    String placeOfBirth, String sex, String address, String phoneNumber,
                    String association, String password, String religion, String civilStatus, String farmingActivity) {
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.extensionName = extensionName;
            this.dateOfBirth = dateOfBirth;
            this.placeOfBirth = placeOfBirth;
            this.sex = sex;

            this.address = address;
            this.phoneNumber = phoneNumber;
            this.association = association;
            this.password = password;
            this.religion = religion;
            this.civilStatus = civilStatus;
            this.farmingActivity = farmingActivity;
        }
    }
}
