package com.example.ligtastanim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OfficersAdapter extends RecyclerView.Adapter<OfficersAdapter.OfficerViewHolder> {
    private List<Officer> officers;

    public OfficersAdapter(List<Officer> officers) {
        this.officers = officers;
    }

    @NonNull
    @Override
    public OfficerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.officer_item, parent, false);
        return new OfficerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfficerViewHolder holder, int position) {
        Officer officer = officers.get(position);
        String firstName = officer.getFirstName() != null ? officer.getFirstName() : "";
        String middleName = officer.getMiddleName() != null ? officer.getMiddleName() : "";
        String lastName = officer.getLastName() != null ? officer.getLastName() : "";
        
        holder.nameText.setText(String.format("%s %s %s", firstName, middleName, lastName).trim());
        holder.positionText.setText(officer.getPosition() != null ? officer.getPosition() : "");
        holder.phoneText.setText(officer.getPhoneNumber() != null ? officer.getPhoneNumber() : "");
    }

    @Override
    public int getItemCount() {
        return officers.size();
    }

    static class OfficerViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, positionText, phoneText;

        OfficerViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.officerName);
            positionText = itemView.findViewById(R.id.officerPosition);
            phoneText = itemView.findViewById(R.id.officerPhone);
        }
    }
} 