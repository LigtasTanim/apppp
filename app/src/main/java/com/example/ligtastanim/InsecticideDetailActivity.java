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

public class InsecticideDetailActivity extends AppCompatActivity {
    private static final String TAG = "InsecticideDetailActivity";

    private ImageView insecticideImageView;
    private TextView insecticideNameTextView;
    private TextView descriptionTextView;
    private RecyclerView referenceImagesRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insecticide_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupRecyclerView();
        loadData();
    }

    private void initializeViews() {
        insecticideImageView = findViewById(R.id.insecticide_image_view);
        insecticideNameTextView = findViewById(R.id.insecticide_name_text_view);
        descriptionTextView = findViewById(R.id.description_text_view);
        referenceImagesRecyclerView = findViewById(R.id.reference_images_recycler_view);
    }

    private void setupRecyclerView() {
        referenceImagesRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadData() {
        String insecticideName = getIntent().getStringExtra("insecticideName");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String description = getIntent().getStringExtra("description");
        ArrayList<String> referenceImageUrls = getIntent().getStringArrayListExtra("referenceImageUrls");

        Log.d(TAG, "Received insecticideName: " + insecticideName);
        Log.d(TAG, "Received referenceImageUrls: " + 
            (referenceImageUrls != null ? referenceImageUrls.size() : "null"));

        insecticideNameTextView.setText(insecticideName);
        descriptionTextView.setText(description);
        Glide.with(this).load(imageUrl).into(insecticideImageView);
    }
}
