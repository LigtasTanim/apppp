package com.example.ligtastanim;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonitoringAdapter extends RecyclerView.Adapter<MonitoringAdapter.ViewHolder> {

    private Context context;
    private List<MonitoringCrops> cropList;
    private String phoneNumber;

    public MonitoringAdapter(Context context, List<MonitoringCrops> cropList, String phoneNumber) {
        this.context = context;
        this.cropList = cropList;
        this.phoneNumber = phoneNumber;
    }

    @NonNull
    @Override
    public MonitoringAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.monitoring_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonitoringAdapter.ViewHolder holder, int position) {
        MonitoringCrops crop = cropList.get(position);
        holder.cropImage.setImageResource(crop.getImageResId());
        holder.cropName.setText(crop.getName());
        holder.startButton.setOnClickListener(v -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("CropsMonitoring").child(crop.getName().toLowerCase());
            String startDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());
                databaseReference.child(phoneNumber).child("MainStatus").setValue("ongoing");
            databaseReference.child(phoneNumber).child("startDate").setValue(startDate);


            if (context instanceof Cmonitoring) {
                ((Cmonitoring) context).checkOngoingMonitoring();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cropImage;
        TextView cropName;
        Button startButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cropImage = itemView.findViewById(R.id.cropImage);
            cropName = itemView.findViewById(R.id.cropName);
            startButton = itemView.findViewById(R.id.startButton);
        }
    }
}
