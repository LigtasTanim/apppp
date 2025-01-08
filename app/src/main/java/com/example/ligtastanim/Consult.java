package com.example.ligtastanim;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.os.Bundle;
import com.example.ligtastanim.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import java.util.Collections;
import android.util.Log;

public class Consult extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private ImageView sendIcon;
    private String phoneNumber;
    private EditText messageBox;
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consult);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        messageBox = findViewById(R.id.messageBox);
        sendIcon = findViewById(R.id.sendIcon);
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(layoutManager);
        messageRecyclerView.setAdapter(messageAdapter);

        sendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageBox.getText().toString().trim();
                if (!TextUtils.isEmpty(message)) {
                    sendMessage(message);
                    messageBox.setText("");
                } else {
                    Toast.makeText(Consult.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadMessages();
    }

    private void sendMessage(String messageText) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Error: Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = databaseReference.push().getKey();
        
        if (messageId != null) {
            databaseReference.child("Farmers").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    String farmerName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                    farmerName = farmerName.trim();
                    if (farmerName.isEmpty()) {
                        farmerName = "Unknown Farmer";
                    }

                    Message message = new Message(
                        messageId,
                        messageText,
                        farmerName,
                        System.currentTimeMillis(),
                        true
                    );
                    
                    DatabaseReference farmerRef = databaseReference.child("Farmers")
                        .child(phoneNumber)
                        .child("messages")
                        .child(messageId);
                    
                    DatabaseReference adminRef = databaseReference.child("Admin")
                        .child(phoneNumber)
                        .child("messages")
                        .child(messageId);
                    
                    farmerRef.setValue(message);
                    adminRef.setValue(message);
                    
                    messageAdapter.addMessage(message);
                    messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                    messageBox.setText("");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(Consult.this, "Error fetching farmer details", Toast.LENGTH_SHORT).show();
                    Log.e("Consult", "Database error: " + databaseError.getMessage());
                }
            });
        } else {
            Toast.makeText(Consult.this, "Error sending message", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages() {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Error: Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference farmerRef = databaseReference.child("Farmers")
            .child(phoneNumber)
            .child("messages");
        
        farmerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }
                
                List<Message> tempMessages = new ArrayList<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null) {
                            tempMessages.add(message);
                            Log.d("Consult", "Added message: " + message.getMessage());
                        }
                    } catch (Exception e) {
                        Log.e("Consult", "Error parsing message: " + e.getMessage());
                    }
                }
                
                Collections.sort(tempMessages, (m1, m2) -> 
                    Long.compare(m1.getTimestamp(), m2.getTimestamp()));
                
                messageList.clear();
                messageList.addAll(tempMessages);
                messageAdapter.notifyDataSetChanged();
                
                if (!messageList.isEmpty()) {
                    messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Consult.this, "Failed to load messages: " + databaseError.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                Log.e("Consult", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public void updateMessages(List<Message> newMessages) {
        messageList.clear();
        messageList.addAll(newMessages);
        messageAdapter.notifyDataSetChanged();
    }
}