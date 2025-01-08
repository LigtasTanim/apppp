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

public class DiseaseDetailActivity extends AppCompatActivity {
    private static final String TAG = "DiseaseDetailActivity";

    private ImageView diseaseImageView;
    private TextView diseaseNameTextView;
    private TextView descriptionTextView;
    private RecyclerView referenceImagesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupRecyclerView();
        loadData();
    }

    private void initializeViews() {
        diseaseImageView = findViewById(R.id.disease_image_view);
        diseaseNameTextView = findViewById(R.id.disease_name_text_view);
        descriptionTextView = findViewById(R.id.description_text_view);
        referenceImagesRecyclerView = findViewById(R.id.reference_images_recycler_view);
    }

    private void setupRecyclerView() {
        referenceImagesRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadData() {
        String diseaseName = getIntent().getStringExtra("diseaseName");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String description = getIntent().getStringExtra("description");
        ArrayList<String> referenceImageUrls = getIntent().getStringArrayListExtra("referenceImageUrls");

        Log.d(TAG, "Received diseaseName: " + diseaseName);
        Log.d(TAG, "Received referenceImageUrls: " + 
            (referenceImageUrls != null ? referenceImageUrls.size() : "null"));

        diseaseNameTextView.setText(diseaseName);
        descriptionTextView.setText(description);
        Glide.with(this).load(imageUrl).into(diseaseImageView);

        if (referenceImageUrls != null && !referenceImageUrls.isEmpty()) {
            ReferenceImagesAdapter adapter = new ReferenceImagesAdapter(this, referenceImageUrls);
            referenceImagesRecyclerView.setAdapter(adapter);
            Log.d(TAG, "Set adapter with " + referenceImageUrls.size() + " images");
        } else {
            Log.d(TAG, "No reference images to display");
        }
    }
}
