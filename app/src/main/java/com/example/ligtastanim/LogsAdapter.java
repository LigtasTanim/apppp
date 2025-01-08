package com.example.ligtastanim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogsViewHolder> {

    private List<String> logsList;

    public LogsAdapter(List<String> logsList) {
        this.logsList = logsList;
    }

    @NonNull
    @Override
    public LogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogsViewHolder holder, int position) {
        String logEntry = logsList.get(position);
        holder.textViewLog.setText(logEntry);
    }

    @Override
    public int getItemCount() {
        return logsList.size();
    }

    public static class LogsViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLog;

        public LogsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLog = itemView.findViewById(R.id.textViewLog);
        }
    }
}
