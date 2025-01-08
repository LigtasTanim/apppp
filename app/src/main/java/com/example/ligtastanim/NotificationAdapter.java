package com.example.ligtastanim;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
    private Context context;
    private String phoneNumber;
    private String firstName;
    private String lastName;

    public NotificationAdapter(Context context, List<Notification> notificationList, String phoneNumber, String firstName, String lastName) {
        this.context = context;
        this.notificationList = notificationList;
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.notificationTitle.setText(notification.getTitle());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnnouncementDetailsActivity.class);
            intent.putExtra("title", notification.getTitle());
            intent.putExtra("body", notification.getBody());
            intent.putExtra("announcementId", notification.getId());
            intent.putExtra("phoneNumber", phoneNumber);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        public TextView notificationTitle;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
        }
    }
}
