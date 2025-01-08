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

public class FertilizerList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FertilizerAdapter fertilizerAdapter;
    private List<Fertilizer> fertilizerList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizer_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        recyclerView = findViewById(R.id.recyclerViewFertilizer);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        fertilizerList = new ArrayList<>();
        fertilizerAdapter = new FertilizerAdapter(fertilizerList, this);
        recyclerView.setAdapter(fertilizerAdapter);


        databaseReference = FirebaseDatabase.getInstance().getReference("Fertilizers");
        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fertilizerList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Fertilizer fertilizer = snapshot.getValue(Fertilizer.class);
                    fertilizerList.add(fertilizer);
                }
                fertilizerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FertilizerListActivity", "Failed to read data from Firebase", databaseError.toException());
            }
        });
    }
}
