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

public class InsecticideAdapter extends RecyclerView.Adapter<InsecticideAdapter.InsecticideViewHolder> {

    private static final String TAG = "InsecticideAdapter";
    private List<Insecticide> insecticides;
    private Context context;

    public InsecticideAdapter(List<Insecticide> insecticides, Context context) {
        this.insecticides = insecticides;
        this.context = context;
    }

    @NonNull
    @Override
    public InsecticideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.insecticide_item, parent, false);
        return new InsecticideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InsecticideViewHolder holder, int position) {
        Insecticide insecticide = insecticides.get(position);
        
        Log.d(TAG, "Binding insecticide: " + insecticide.getInsecticideName());
        Log.d(TAG, "Reference URLs size: " + 
            (insecticide.getReferenceImageUrls() != null ? insecticide.getReferenceImageUrls().size() : "null"));

        holder.insecticideName.setText(insecticide.getInsecticideName());
        Glide.with(context)
                .load(insecticide.getImageUrl())
                .into(holder.insecticideImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, InsecticideDetailActivity.class);
                intent.putExtra("insecticideName", insecticide.getInsecticideName());
                intent.putExtra("imageUrl", insecticide.getImageUrl());
                intent.putExtra("description", insecticide.getDescription());
                intent.putStringArrayListExtra("referenceImageUrls", 
                    new ArrayList<>(insecticide.getReferenceImageUrls()));
                
                Log.d(TAG, "Starting InsecticideDetailActivity with " + 
                    insecticide.getReferenceImageUrls().size() + " reference images");
                
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return insecticides.size();
    }

    static class InsecticideViewHolder extends RecyclerView.ViewHolder {
        ImageView insecticideImage;
        TextView insecticideName;

        public InsecticideViewHolder(@NonNull View itemView) {
            super(itemView);
            insecticideImage = itemView.findViewById(R.id.item_insecticide_image);
            insecticideName = itemView.findViewById(R.id.item_insecticide_name);
        }
    }
}
