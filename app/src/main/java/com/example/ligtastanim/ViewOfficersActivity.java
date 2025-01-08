package com.example.ligtastanim;

import android.os.Bundle;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import android.widget.Toast;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class ViewOfficersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private TextView associationNameText;
    private String associationId, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_officers);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.officersRecyclerView);
        associationNameText = findViewById(R.id.associationNameText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        associationId = getIntent().getStringExtra("association");
        Log.d("ViewOfficers", "Received association ID: " + associationId);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        loadAssociationName();
        
        loadOfficers();
    }

    private void loadAssociationName() {
        Log.d("ViewOfficers", "Loading association name for ID: " + associationId);
        databaseReference.child("FarmersAssociation").child(associationId)
            .child("associationName")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        Log.d("ViewOfficers", "Found association name: " + name);
                        associationNameText.setText(name);
                    } else {
                        Log.d("ViewOfficers", "No association name found for ID: " + associationId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ViewOfficersActivity.this, "Error loading association name", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void loadOfficers() {
        Log.d("ViewOfficers", "Loading officers using phone number: " + phoneNumber);
        databaseReference.child("Farmers").child(phoneNumber)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String associationName = snapshot.child("association").getValue(String.class);
                        Log.d("ViewOfficers", "Found farmer's association: " + associationName);

                        if (associationName != null && !associationName.isEmpty()) {
                            associationNameText.setText(associationName);

                            // Query the specific association node directly
                            databaseReference.child("FarmersAssociation")
                                .child(associationName)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        List<Officer> officersList = new ArrayList<>();
                                        Log.d("ViewOfficers", "Found " + snapshot.getChildrenCount() + 
                                              " officers in association: " + associationName);

                                        for (DataSnapshot officerSnapshot : snapshot.getChildren()) {
                                            // Get officer details
                                            String phoneNum = officerSnapshot.child("phoneNumber").getValue(String.class);
                                            String firstName = officerSnapshot.child("firstname").getValue(String.class);
                                            String middleName = officerSnapshot.child("middlename").getValue(String.class);
                                            String lastName = officerSnapshot.child("lastname").getValue(String.class);
                                            String position = officerSnapshot.child("position").getValue(String.class);
                                            Boolean archived = officerSnapshot.child("archived").getValue(Boolean.class);

                                            // Only add non-archived officers
                                            if (archived == null || !archived) {
                                                Officer officer = new Officer();
                                                officer.setPhoneNumber(phoneNum);
                                                officer.setFirstName(firstName);
                                                officer.setMiddleName(middleName);
                                                officer.setLastName(lastName);
                                                officer.setPosition(position);
                                                officersList.add(officer);
                                                Log.d("ViewOfficers", "Added officer: " + firstName + " " + 
                                                      lastName + " - " + position);
                                            }
                                        }

                                        // Sort officers by position rank
                                        Collections.sort(officersList, new Comparator<Officer>() {
                                            @Override
                                            public int compare(Officer o1, Officer o2) {
                                                return Integer.compare(o1.getPositionRank(), o2.getPositionRank());
                                            }
                                        });

                                        if (officersList.isEmpty()) {
                                            Log.d("ViewOfficers", "No active officers found");
                                            Toast.makeText(ViewOfficersActivity.this, 
                                                "No active officers found in this association", 
                                                Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.d("ViewOfficers", "Setting adapter with " + 
                                                  officersList.size() + " sorted officers");
                                            OfficersAdapter adapter = new OfficersAdapter(officersList);
                                            recyclerView.setAdapter(adapter);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("ViewOfficers", "Error loading officers: " + error.getMessage());
                                        Toast.makeText(ViewOfficersActivity.this, 
                                            "Error loading officers", 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } else {
                            Log.d("ViewOfficers", "No association found for farmer");
                            Toast.makeText(ViewOfficersActivity.this, 
                                "No association found for this farmer", 
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ViewOfficers", "Error loading farmer data: " + error.getMessage());
                    Toast.makeText(ViewOfficersActivity.this, 
                        "Error loading farmer data", 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
}