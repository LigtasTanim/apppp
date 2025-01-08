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

public class InsecticideList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InsecticideAdapter insecticideAdapter;
    private List<Insecticide> insecticideList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insecticide_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        recyclerView = findViewById(R.id.recyclerViewInsecticide);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        insecticideList = new ArrayList<>();
        insecticideAdapter = new InsecticideAdapter(insecticideList, this);
        recyclerView.setAdapter(insecticideAdapter);
        databaseReference = FirebaseDatabase.getInstance().getReference("Insecticides");

        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                insecticideList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Insecticide insecticide = snapshot.getValue(Insecticide.class);
                    insecticideList.add(insecticide);
                }
                insecticideAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("InsecticideListActivity", "Failed to read data from Firebase", databaseError.toException());
            }
        });
    }
}
