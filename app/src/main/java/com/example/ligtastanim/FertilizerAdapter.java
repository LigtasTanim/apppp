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

public class FertilizerAdapter extends RecyclerView.Adapter<FertilizerAdapter.FertilizerViewHolder> {

    private static final String TAG = "FertilizerAdapter";
    private List<Fertilizer> fertilizers;
    private Context context;

    public FertilizerAdapter(List<Fertilizer> fertilizers, Context context) {
        this.fertilizers = fertilizers;
        this.context = context;
    }

    @NonNull
    @Override
    public FertilizerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fertilizer_item, parent, false);
        return new FertilizerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FertilizerViewHolder holder, int position) {
        Fertilizer fertilizer = fertilizers.get(position);
        
        Log.d(TAG, "Binding fertilizer: " + fertilizer.getFertilizerName());
        Log.d(TAG, "Reference URLs size: " + 
            (fertilizer.getReferenceImageUrls() != null ? fertilizer.getReferenceImageUrls().size() : "null"));

        holder.fertilizerName.setText(fertilizer.getFertilizerName());
        Glide.with(context)
                .load(fertilizer.getImageUrl())
                .into(holder.fertilizerImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FertilizerDetailActivity.class);
                intent.putExtra("fertilizerName", fertilizer.getFertilizerName());
                intent.putExtra("imageUrl", fertilizer.getImageUrl());
                intent.putExtra("description", fertilizer.getDescription());
                intent.putStringArrayListExtra("referenceImageUrls", 
                    new ArrayList<>(fertilizer.getReferenceImageUrls()));
                
                Log.d(TAG, "Starting FertilizerDetailActivity with " + 
                    fertilizer.getReferenceImageUrls().size() + " reference images");
                
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fertilizers.size();
    }

    static class FertilizerViewHolder extends RecyclerView.ViewHolder {
        ImageView fertilizerImage;
        TextView fertilizerName;

        public FertilizerViewHolder(@NonNull View itemView) {
            super(itemView);
            fertilizerImage = itemView.findViewById(R.id.item_fertilizer_image);
            fertilizerName = itemView.findViewById(R.id.item_fertilizer_name);
        }
    }
}
