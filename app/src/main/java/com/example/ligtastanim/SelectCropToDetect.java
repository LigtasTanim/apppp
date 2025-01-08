package com.example.ligtastanim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;


import androidx.appcompat.app.AppCompatActivity;


public class SelectCropToDetect extends AppCompatActivity {

    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_crop_to_detect);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        LinearLayout cornDetection = findViewById(R.id.linearLayout3);
        cornDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDetectionCorn();
            }
        });

        LinearLayout ampalayaDetection = findViewById(R.id.linearLayout1);
        ampalayaDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDetectionAmpalaya();
            }
        });
        LinearLayout riceDetection = findViewById(R.id.linearLayout2);
        riceDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDetectionRice();
            }
        });
    }

    private void goToDetectionCorn() {
        Intent intent = new Intent(SelectCropToDetect.this, Detection.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

    private void goToDetectionAmpalaya() {
        Intent intent = new Intent(SelectCropToDetect.this, Detection3.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

    private void goToDetectionRice() {
        Intent intent = new Intent(SelectCropToDetect.this, Detection2.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

}