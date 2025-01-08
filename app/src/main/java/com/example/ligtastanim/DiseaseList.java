package com.example.ligtastanim;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DiseaseList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DiseaseAdapter diseaseAdapter;
    private List<Disease> diseaseList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        recyclerView = findViewById(R.id.recyclerViewDisease);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        diseaseList = new ArrayList<>();
        diseaseAdapter = new DiseaseAdapter(diseaseList, this);
        recyclerView.setAdapter(diseaseAdapter);


        databaseReference = FirebaseDatabase.getInstance().getReference("Diseases");


        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                diseaseList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Disease disease = snapshot.getValue(Disease.class);
                    diseaseList.add(disease);
                }
                diseaseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DiseaseListActivity", "Failed to read data from Firebase", databaseError.toException());
            }
        });
    }
}
