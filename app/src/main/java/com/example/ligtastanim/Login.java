package com.example.ligtastanim;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;
    private TextView textViewForgotPassword;
    private static final String TAG = "LoginActivity";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewForgotPassword.setVisibility(View.GONE);
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


        TextView textViewRegister = findViewById(R.id.textViewRegister);
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });


        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);


        TextView consult = findViewById(R.id.textViewForgotPassword);
        consult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { goToReset(); }
        });
    }

    private void goToReset() {
        Intent intent = new Intent(Login.this, ResetPass.class);
        startActivity(intent);
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

    private void loginUser() {
        String phoneNumber = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String[] status = new String[1];

        if (!phoneNumber.startsWith("+63") || phoneNumber.length() != 13) {
            Toast.makeText(this, "Phone number must start with +63 and be 13 digits long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();


        mDatabase.child("Farmers").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                if (dataSnapshot.exists()) {
                    String storedHashedPassword = dataSnapshot.child("password").getValue(String.class);

                    if (storedHashedPassword != null) {
                        Log.d(TAG, "Stored hashed password: " + storedHashedPassword);
                        String hashedInputPassword = hashPassword(password);
                        Log.d(TAG, "Hashed input password: " + hashedInputPassword);

                        if (storedHashedPassword.equals(hashedInputPassword)) {
                            Log.d(TAG, "Login successful for user: " + phoneNumber);
                            Toast.makeText(Login.this, "Login successful for user: " + phoneNumber, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, Home.class);
                            intent.putExtra("phoneNumber", phoneNumber);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Invalid phone number or password", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Invalid password for phone number: " + phoneNumber);
                        }
                    } else {
                        Toast.makeText(Login.this, "Error retrieving password", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Stored password is null for phone number: " + phoneNumber);
                    }
                } else {
                    Toast.makeText(Login.this, "User does not exist", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "User does not exist: " + phoneNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(Login.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error: ", databaseError.toException());
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
            throw new RuntimeException(e);
        }
    }
}