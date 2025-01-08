package com.example.ligtastanim;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OTPpage extends AppCompatActivity {

    private static final String TAG = "OTPpage";
    private static final int EXPECTED_OTP_LENGTH = 6;
    private String phoneNumber;
    private String verificationCode;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private EditText otpInput;
    private Button nextBtn;
    private ProgressBar progressBar;
    private TextView resendOtpTextView;
    private FirebaseAuth mAuth;

    private long timeoutSeconds = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otppage);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        otpInput = findViewById(R.id.otpInput);
        nextBtn = findViewById(R.id.verifyOtpButton);
        progressBar = findViewById(R.id.login_progress_bar);
        resendOtpTextView = findViewById(R.id.resend_otp_textview);

        phoneNumber = getIntent().getExtras().getString("phone");
        sendOtp(phoneNumber, false);

        nextBtn.setOnClickListener(v -> {
            String enteredOtp = otpInput.getText().toString();
            if (enteredOtp.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter the OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            if (enteredOtp.length() != EXPECTED_OTP_LENGTH) {
                Toast.makeText(getApplicationContext(), "Invalid OTP length", Toast.LENGTH_SHORT).show();
                return;
            }

            if (verificationCode == null) {
                Toast.makeText(getApplicationContext(), "OTP not yet sent. Please wait.", Toast.LENGTH_SHORT).show();
                return;
            }

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp);
            signIn(credential);
        });

        resendOtpTextView.setOnClickListener((v) -> {
            sendOtp(phoneNumber, true);
        });
    }

    private void sendOtp(String phoneNumber, boolean isResend) {
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+" + phoneNumber;
        }

        startResendTimer();
        setInProgress(true);

        Log.d(TAG, "Sending OTP to " + phoneNumber);

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        Log.d(TAG, "Verification completed automatically");
                        signIn(phoneAuthCredential);
                        setInProgress(false);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        String errorMessage;
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Invalid phone number format. Please use format: +[country code][number]";
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            errorMessage = "Too many requests. Please try again later.";
                        } else {
                            errorMessage = "Verification failed: " + e.getMessage();
                        }
                        Log.e(TAG, "Verification failed", e);
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                        setInProgress(false);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                         @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        super.onCodeSent(verificationId, token);
                        Log.d(TAG, "Code sent successfully");
                        verificationCode = verificationId;
                        resendingToken = token;
                        Toast.makeText(getApplicationContext(), "OTP sent successfully", Toast.LENGTH_SHORT).show();
                        setInProgress(false);
                    }
                });

        if (isResend) {
            if (resendingToken != null) {
                Log.d(TAG, "Resending OTP");
                PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
            } else {
                Log.e(TAG, "Cannot resend OTP. Resending token is null.");
                Toast.makeText(getApplicationContext(), "Cannot resend OTP. Please try again later.", Toast.LENGTH_SHORT).show();
                setInProgress(false);
            }
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            nextBtn.setVisibility(View.VISIBLE);
        }
    }

    private void signIn(PhoneAuthCredential phoneAuthCredential) {
        setInProgress(true);
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Successfully signed in!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OTPpage.this, Login.class);
                intent.putExtra("phone", phoneNumber);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "OTP verification failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startResendTimer() {
        resendOtpTextView.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    timeoutSeconds--;
                    resendOtpTextView.setText("Resend OTP in " + timeoutSeconds + " seconds");
                    if (timeoutSeconds <= 0) {
                        timeoutSeconds = 60;
                        timer.cancel();
                        resendOtpTextView.setText("Resend OTP");
                        resendOtpTextView.setEnabled(true);
                    }
                });
            }
        }, 0, 1000);
    }
}