package com.example.ligtastanim;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.view.View;


public class TransferAssociationActivity extends AppCompatActivity {

    private Spinner reasonSpinner, associationSpinner;
    private Button transferButton;
    private DatabaseReference farmersRef, associationRef;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_association);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");

        reasonSpinner = findViewById(R.id.reasonSpinner);
        associationSpinner = findViewById(R.id.associationSpinner);
        transferButton = findViewById(R.id.transferButton);

        farmersRef = FirebaseDatabase.getInstance().getReference("Farmers");
        associationRef = FirebaseDatabase.getInstance().getReference("association");

        setupReasonSpinner();
        loadAssociations();

        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferFarmer();
            }
        });
    }

    private void setupReasonSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transfer_reasons, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reasonSpinner.setAdapter(adapter);
    }

    private void loadAssociations() {
        associationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> associations = new ArrayList<>();
                for (DataSnapshot assocSnapshot : snapshot.getChildren()) {
                    associations.add(assocSnapshot.getKey());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(TransferAssociationActivity.this, android.R.layout.simple_spinner_dropdown_item, associations);
                associationSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransferAssociationActivity.this, "Failed to load associations.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void transferFarmer() {
        String selectedReason = reasonSpinner.getSelectedItem().toString();
        String selectedAssociation = associationSpinner.getSelectedItem().toString();

        
        farmersRef.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String farmerFullName = firstName + " " + lastName; 

                    
                    String currentAssociation = snapshot.child("selectedAssociation").getValue(String.class);

                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("association", selectedAssociation);
                    updates.put("status", "Pending");
                    farmersRef.child(phoneNumber).updateChildren(updates);

                    
                    if (currentAssociation != null) {
                        associationRef.child(currentAssociation).child(phoneNumber).removeValue();

                        
                        DatabaseReference historyRef = associationRef.child(currentAssociation)
                                .child("association history").push();
                        Map<String, Object> log = new HashMap<>();
                        log.put("action", "Transferred");
                        log.put("details", "Transferred association: " + farmerFullName + " for reason: " + selectedReason);
                        log.put("fullname", farmerFullName);
                        log.put("timestamp", System.currentTimeMillis());
                        historyRef.setValue(log);
                    }

                    Toast.makeText(TransferAssociationActivity.this, "Farmer transferred successfully.", Toast.LENGTH_SHORT).show();

                    
                    Intent intent = new Intent(TransferAssociationActivity.this, Home.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(TransferAssociationActivity.this, "Farmer not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransferAssociationActivity.this, "Failed to transfer farmer.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}