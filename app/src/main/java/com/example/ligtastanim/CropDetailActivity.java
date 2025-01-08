package com.example.ligtastanim;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class CropDetailActivity extends AppCompatActivity {
    private static final String TAG = "CropDetailActivity";

    private ImageView cropImageView;
    private TextView cropNameTextView;
    private TextView description1TextView;
    private TextView description2TextView;
    private RecyclerView referenceImagesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupRecyclerView();
        loadData();
    }

    private void initializeViews() {
        cropImageView = findViewById(R.id.crop_image_view);
        cropNameTextView = findViewById(R.id.crop_name_text_view);
        description1TextView = findViewById(R.id.description1_text_view);
        description2TextView = findViewById(R.id.description2_text_view);
        referenceImagesRecyclerView = findViewById(R.id.reference_images_recycler_view);
    }

    private void setupRecyclerView() {
        referenceImagesRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadData() {
        String cropName = getIntent().getStringExtra("cropName");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String description1 = getIntent().getStringExtra("description1");
        String description2 = getIntent().getStringExtra("description2");
        ArrayList<String> referenceImageUrls = getIntent().getStringArrayListExtra("referenceImageUrls");

        Log.d(TAG, "Received cropName: " + cropName);
        Log.d(TAG, "Received referenceImageUrls: " + 
            (referenceImageUrls != null ? referenceImageUrls.size() : "null"));

        cropNameTextView.setText(cropName);
        description1TextView.setText(description1);
        description2TextView.setText(description2);
        Glide.with(this).load(imageUrl).into(cropImageView);

        if (referenceImageUrls != null && !referenceImageUrls.isEmpty()) {
            ReferenceImagesAdapter adapter = new ReferenceImagesAdapter(this, referenceImageUrls);
            referenceImagesRecyclerView.setAdapter(adapter);
            Log.d(TAG, "Set adapter with " + referenceImageUrls.size() + " images");
        } else {
            Log.d(TAG, "No reference images to display");
        }
    }
}