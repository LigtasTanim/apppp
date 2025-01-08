package com.example.ligtastanim;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder> {

    private static final String TAG = "DiseaseAdapter";
    private List<Disease> diseases;
    private Context context;

    public DiseaseAdapter(List<Disease> diseases, Context context) {
        this.diseases = diseases;
        this.context = context;
    }

    @NonNull
    @Override
    public DiseaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.disease_item, parent, false);
        return new DiseaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiseaseViewHolder holder, int position) {
        Disease disease = diseases.get(position);
        
        Log.d(TAG, "Binding disease: " + disease.getDiseaseName());
        Log.d(TAG, "Reference URLs size: " + 
            (disease.getReferenceImageUrls() != null ? disease.getReferenceImageUrls().size() : "null"));

        holder.diseaseName.setText(disease.getDiseaseName());
        Glide.with(context)
                .load(disease.getImageUrl())
                .into(holder.diseaseImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DiseaseDetailActivity.class);
                intent.putExtra("diseaseName", disease.getDiseaseName());
                intent.putExtra("imageUrl", disease.getImageUrl());
                intent.putExtra("description", disease.getDescription());
                intent.putStringArrayListExtra("referenceImageUrls", 
                    new ArrayList<>(disease.getReferenceImageUrls()));
                
                Log.d(TAG, "Starting DiseaseDetailActivity with " + 
                    disease.getReferenceImageUrls().size() + " reference images");
                
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return diseases.size();
    }

    static class DiseaseViewHolder extends RecyclerView.ViewHolder {
        ImageView diseaseImage;
        TextView diseaseName;

        public DiseaseViewHolder(@NonNull View itemView) {
            super(itemView);
            diseaseImage = itemView.findViewById(R.id.item_disease_image);
            diseaseName = itemView.findViewById(R.id.item_disease_name);
        }
    }
}
