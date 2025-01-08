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

public class PestAdapter extends RecyclerView.Adapter<PestAdapter.PestViewHolder> {

    private static final String TAG = "PestAdapter";
    private List<Pest> pests;
    private Context context;

    public PestAdapter(List<Pest> pests, Context context) {
        this.pests = pests;
        this.context = context;
    }

    @NonNull
    @Override
    public PestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pest_item, parent, false);
        return new PestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PestViewHolder holder, int position) {
        Pest pest = pests.get(position);
        
        Log.d(TAG, "Binding pest: " + pest.getPestName());
        Log.d(TAG, "Reference URLs size: " + 
            (pest.getReferenceImageUrls() != null ? pest.getReferenceImageUrls().size() : "null"));

        holder.pestName.setText(pest.getPestName());
        Glide.with(context)
                .load(pest.getImageUrl())
                .into(holder.pestImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PestDetailActivity.class);
                intent.putExtra("pestName", pest.getPestName());
                intent.putExtra("imageUrl", pest.getImageUrl());
                intent.putExtra("description1", pest.getDescription1());
                intent.putExtra("description2", pest.getDescription2());
                intent.putStringArrayListExtra("referenceImageUrls", 
                    new ArrayList<>(pest.getReferenceImageUrls()));
                
                Log.d(TAG, "Starting PestDetailActivity with " + 
                    pest.getReferenceImageUrls().size() + " reference images");
                
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pests.size();
    }

    static class PestViewHolder extends RecyclerView.ViewHolder {
        ImageView pestImage;
        TextView pestName;

        public PestViewHolder(@NonNull View itemView) {
            super(itemView);
            pestImage = itemView.findViewById(R.id.item_pest_image);
            pestName = itemView.findViewById(R.id.item_pest_name);
        }
    }
}
