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

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {

    private static final String TAG = "CropAdapter";
    private List<Crop> crops;
    private Context context;

    public CropAdapter(List<Crop> crops, Context context) {
        this.crops = crops;
        this.context = context;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.crop_item, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        Crop crop = crops.get(position);
        
        Log.d(TAG, "Binding crop: " + crop.getCropName());
        Log.d(TAG, "Reference URLs size: " + 
            (crop.getReferenceImageUrls() != null ? crop.getReferenceImageUrls().size() : "null"));

        holder.cropName.setText(crop.getCropName());
        Glide.with(context)
                .load(crop.getImageUrl())
                .into(holder.cropImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CropDetailActivity.class);
                intent.putExtra("cropName", crop.getCropName());
                intent.putExtra("imageUrl", crop.getImageUrl());
                intent.putExtra("description1", crop.getDescription1());
                intent.putExtra("description2", crop.getDescription2());
                intent.putStringArrayListExtra("referenceImageUrls", 
                    new ArrayList<>(crop.getReferenceImageUrls()));
                
                Log.d(TAG, "Starting CropDetailActivity with " + 
                    crop.getReferenceImageUrls().size() + " reference images");
                
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return crops.size();
    }

    static class CropViewHolder extends RecyclerView.ViewHolder {
        ImageView cropImage;
        TextView cropName;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropImage = itemView.findViewById(R.id.item_crop_image);
            cropName = itemView.findViewById(R.id.item_crop_name);
        }
    }
}
