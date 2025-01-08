package com.example.ligtastanim;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetectionHistoryAdapter extends RecyclerView.Adapter<DetectionHistoryAdapter.ViewHolder> {
    private List<DetectionHistoryItem> detectionList;
    private Context context;

    public DetectionHistoryAdapter(Context context) {
        this.context = context;
        this.detectionList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detection_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetectionHistoryItem item = detectionList.get(position);
        holder.classificationText.setText(item.getClassification());
        
        // Format the timestamp
        Date date = new Date(item.getTimestamp());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        holder.timestampText.setText(sdf.format(date));
    }

    @Override
    public int getItemCount() {
        return detectionList.size();
    }

    public void updateData(List<DetectionHistoryItem> newData) {
        detectionList.clear();
        detectionList.addAll(newData);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView classificationText;
        TextView timestampText;
        ImageView pestIcon;

        ViewHolder(View itemView) {
            super(itemView);
            classificationText = itemView.findViewById(R.id.classificationText);
            timestampText = itemView.findViewById(R.id.timestampText);
            pestIcon = itemView.findViewById(R.id.pestIcon);
        }
    }
} 