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

public class PestList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PestAdapter pestAdapter;
    private List<Pest> pestList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pest_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        recyclerView = findViewById(R.id.recyclerViewPest);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        pestList = new ArrayList<>();
        pestAdapter = new PestAdapter(pestList, this);
        recyclerView.setAdapter(pestAdapter);
        databaseReference = FirebaseDatabase.getInstance().getReference("Pests");
        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pestList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pest pest = snapshot.getValue(Pest.class);
                    pestList.add(pest);
                }
                pestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PestListActivity", "Failed to read data from Firebase", databaseError.toException());
            }
        });
    }
}
