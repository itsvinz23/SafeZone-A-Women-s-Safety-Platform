package com.s23010921.safezone;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SafePlace extends AppCompatActivity {

    private Button btnSearch, btnNext;
    private TextView txtSearch;
    private int currentIndex = 0;

    // Array of place types to search
    private final String[] places = {
            "police station near me",
            "hospital near me",
            "temple near me",
            "church near me",
            "pharmacy near me"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_place);

        // Initialize UI components
        btnSearch = findViewById(R.id.btnSearch);
        btnNext = findViewById(R.id.btnNext);
        txtSearch = findViewById(R.id.etSearch);

        // Set initial place text
        txtSearch.setText(places[currentIndex]);

        // Handle Next button
        btnNext.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % places.length;
            txtSearch.setText(places[currentIndex]);
        });

        // Handle Search button
        btnSearch.setOnClickListener(v -> {
            String query = places[currentIndex];
            try {
                Uri uri = Uri.parse("https://www.google.com/maps/search/" + Uri.encode(query));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Google Maps app not found", Toast.LENGTH_SHORT).show();
            }
        });
        // Contact List icon click
        ImageView homeIcon = findViewById(R.id.homeIcon);
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SafePlace.this, DashBoard.class);
            startActivity(intent);
        });
    }
}
