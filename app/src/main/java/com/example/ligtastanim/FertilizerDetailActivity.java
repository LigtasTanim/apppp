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

public class FertilizerDetailActivity extends AppCompatActivity {
    private static final String TAG = "FertilizerDetailActivity";

    private ImageView fertilizerImageView;
    private TextView fertilizerNameTextView;
    private TextView descriptionTextView;
    private RecyclerView referenceImagesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizer_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupRecyclerView();
        loadData();
    }

    private void initializeViews() {
        fertilizerImageView = findViewById(R.id.fertilizer_image_view);
        fertilizerNameTextView = findViewById(R.id.fertilizer_name_text_view);
        descriptionTextView = findViewById(R.id.description_text_view);
        referenceImagesRecyclerView = findViewById(R.id.reference_images_recycler_view);
    }

    private void setupRecyclerView() {
        referenceImagesRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadData() {
        String fertilizerName = getIntent().getStringExtra("fertilizerName");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String description = getIntent().getStringExtra("description");
        ArrayList<String> referenceImageUrls = getIntent().getStringArrayListExtra("referenceImageUrls");

        Log.d(TAG, "Received fertilizerName: " + fertilizerName);
        Log.d(TAG, "Received referenceImageUrls: " + 
            (referenceImageUrls != null ? referenceImageUrls.size() : "null"));

        fertilizerNameTextView.setText(fertilizerName);
        descriptionTextView.setText(description);
        Glide.with(this).load(imageUrl).into(fertilizerImageView);

        if (referenceImageUrls != null && !referenceImageUrls.isEmpty()) {
            ReferenceImagesAdapter adapter = new ReferenceImagesAdapter(this, referenceImageUrls);
            referenceImagesRecyclerView.setAdapter(adapter);
            Log.d(TAG, "Set adapter with " + referenceImageUrls.size() + " images");
        } else {
            Log.d(TAG, "No reference images to display");
        }
    }
}
