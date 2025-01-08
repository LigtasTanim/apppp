package com.example.ligtastanim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChatList extends AppCompatActivity {

    private CardView cardDOA, cardFA;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        cardDOA = findViewById(R.id.cardDOA);
        cardFA = findViewById(R.id.cardFA);
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        CardView cardDOA = findViewById(R.id.cardDOA);
        cardDOA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToDA();
            }
        });

        CardView cardFA = findViewById(R.id.cardFA);
        cardFA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFA();
            }
        });
    }

    private void goToDA() {
        Intent intent = new Intent(ChatList.this, Consult.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }

    private void goToFA() {
        Intent intent = new Intent(ChatList.this, FAConsult.class);
        if (phoneNumber != null) {
            intent.putExtra("phoneNumber", phoneNumber);
        }
        startActivity(intent);
    }
}