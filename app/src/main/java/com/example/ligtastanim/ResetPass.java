package com.example.ligtastanim;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class ResetPass extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    // Stages: 1 = Phone Input, 2 = OTP Verification, 3 = New Password
    private int stage = 1;

    EditText phoneInput, otpInput, newPasswordInput, confirmPasswordInput;
    Button actionButton;
    ProgressBar progressBar;
    TextView infoText;

    String verificationId;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Farmers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI components
        phoneInput = findViewById(R.id.phoneInput);
        otpInput = findViewById(R.id.otpInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        actionButton = findViewById(R.id.actionButton);
        progressBar = findViewById(R.id.progressBar);
        infoText = findViewById(R.id.infoText);

        updateUI();

        actionButton.setOnClickListener(v -> {
            if (stage == 1) {
                handlePhoneInput();
            } else if (stage == 2) {
                handleOtpVerification();
            } else if (stage == 3) {
                handlePasswordReset();
            }
        });
    }

    private void updateUI() {
        phoneInput.setVisibility(stage == 1 ? View.VISIBLE : View.GONE);
        otpInput.setVisibility(stage == 2 ? View.VISIBLE : View.GONE);
        newPasswordInput.setVisibility(stage == 3 ? View.VISIBLE : View.GONE);
        confirmPasswordInput.setVisibility(stage == 3 ? View.VISIBLE : View.GONE);

        infoText.setText(stage == 1
                ? "Enter your phone number to reset password"
                : stage == 2
                ? "Enter the OTP sent to your phone"
                : "Enter your new password");

        actionButton.setText(stage == 1
                ? "Send OTP"
                : stage == 2
                ? "Verify OTP"
                : "Reset Password");
    }

    private void handlePhoneInput() {
        String phoneNumber = phoneInput.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        sendOtp(phoneNumber);
    }

    private void sendOtp(String phoneNumber) {
        setInProgress(true); // Show progress indicator

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth) // Create PhoneAuthOptions instance
                .setPhoneNumber(phoneNumber) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout duration
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Log.d(TAG, "OTP automatically verified");
                        signInWithCredential(credential); // Automatically sign in if verification succeeds
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e(TAG, "OTP sending failed", e);
                        Toast.makeText(ResetPass.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        setInProgress(false); // Hide progress indicator
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        ResetPass.this.verificationId = verificationId; // Save verification ID
                        resendingToken = token; // Save resending token
                        Log.d(TAG, "OTP sent: " + verificationId);
                        Toast.makeText(ResetPass.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                        stage = 2; // Proceed to the next stage (e.g., entering a new password)
                        updateUI();
                        setInProgress(false); // Hide progress indicator
                    }
                })
                .build(); // Build the PhoneAuthOptions object

        PhoneAuthProvider.verifyPhoneNumber(options); // Trigger phone number verification
    }

    private void handleOtpVerification() {
        String otp = otpInput.getText().toString().trim();
        if (otp.isEmpty()) {
            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        verifyOtp(otp);
    }

    private void verifyOtp(String otp) {
        setInProgress(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "OTP verified successfully", Toast.LENGTH_SHORT).show();
                        stage = 3;
                        updateUI();
                    } else {
                        Toast.makeText(this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handlePasswordReset() {
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        updatePassword(newPassword);
    }

    private void updatePassword(String newPassword) {
        // Hash the new password before saving it
        String hashedPassword = hashPassword(newPassword);

        // Get the current user's phone number
        String phoneNumber = phoneInput.getText().toString().trim();

        // Update password in Realtime Database
        DatabaseReference userRef = databaseReference.child(phoneNumber).child("password");
        userRef.setValue(hashedPassword)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString(); // Return hashed password
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        actionButton.setEnabled(!inProgress);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                stage = 3;
                updateUI();
            } else {
                Toast.makeText(this, "Verification failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            setInProgress(false);
        });
    }
}
