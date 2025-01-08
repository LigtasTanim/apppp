package com.example.ligtastanim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.ligtastanim.Comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnnouncementDetailsActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView bodyTextView;
    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private Button submitComment;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private String announcementId;
    private String phoneNumber;
    private DatabaseReference commentsRef;
    private final Set<String> curseWords = new HashSet<>(Arrays.asList(
        "putangina", "tangina", "gago", "puta", "bobo", "tanga", "ulol", "bubu", "gaga", "baonimam", "baoninayu", "kitnayu", "iyot",
        "tangina mo", "tangina nyo", "pakyu", "punyeta", "pakyo", "bagtit", "tang ina", "putang ina",
        "ukinnam", "kitnam", "kinnam", "ukinnayo", "tarantado", "pota", "potangina", "gunggong", "uki", "salsal"
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        titleTextView = findViewById(R.id.titleTextView);
        bodyTextView = findViewById(R.id.bodyTextView);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        announcementId = intent.getStringExtra("announcementId");
        phoneNumber = intent.getStringExtra("phoneNumber");

        // Initialize Firebase
        commentsRef = FirebaseDatabase.getInstance().getReference()
            .child("Announcement")
            .child(announcementId)
            .child("comments");

        // Setup comment section
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        submitComment = findViewById(R.id.submitComment);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);

        submitComment.setOnClickListener(v -> submitNewComment());
        loadComments();

        titleTextView.setText(title);
        bodyTextView.setText(body);

        // Add this code to handle the View All button
        Button viewAllCommentsBtn = findViewById(R.id.viewAllCommentsBtn);
        viewAllCommentsBtn.setOnClickListener(v -> {
            Intent intents = new Intent(AnnouncementDetailsActivity.this, AllCommentsActivity.class);
            intents.putExtra("announcementId", announcementId);
            startActivity(intents);
        });
    }

    private boolean containsCurseWords(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            if (curseWords.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private void submitNewComment() {
        String commentText = commentInput.getText().toString().trim();
        if (!commentText.isEmpty()) {
            if (containsCurseWords(commentText)) {
                Toast.makeText(AnnouncementDetailsActivity.this,
                    "Please keep comments respectful. Inappropriate language is not allowed.",
                    Toast.LENGTH_LONG).show();
                return;
            }

            DatabaseReference farmerRef = FirebaseDatabase.getInstance().getReference()
                .child("Farmers")
                .child(phoneNumber);

            farmerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    
                    if (firstName == null) firstName = "";
                    if (lastName == null) lastName = "";
                    String userName = (firstName + " " + lastName).trim();
                    
                    if (userName.isEmpty()) {
                        userName = phoneNumber;
                    }

                    String commentId = commentsRef.push().getKey();
                    Comment comment = new Comment(
                        commentId, 
                        phoneNumber, 
                        userName,
                        commentText, 
                        System.currentTimeMillis()
                    );
                    
                    if (commentId != null) {
                        commentsRef.child(commentId).setValue(comment)
                            .addOnSuccessListener(aVoid -> {
                                commentInput.setText("");
                                Toast.makeText(AnnouncementDetailsActivity.this, 
                                    "Comment posted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> 
                                Toast.makeText(AnnouncementDetailsActivity.this, 
                                    "Failed to post comment", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AnnouncementDetailsActivity.this, 
                        "Failed to get farmer information", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AnnouncementDetailsActivity.this, 
                    "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
