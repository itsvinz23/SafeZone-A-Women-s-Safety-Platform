package com.s23010921.safezone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddFeedBack extends AppCompatActivity {

    private EditText etRating, etExperience, etSuggestions;
    private Button btnSendFeedback;

    private ImageView homeIcon,feedbackIcon;
    private DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_feed_back);

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI
        etRating = findViewById(R.id.etRating);
        etExperience = findViewById(R.id.etExperience);
        etSuggestions = findViewById(R.id.etSuggestions);
        btnSendFeedback = findViewById(R.id.btnSendFeedback);

        // Firebase reference for "feedbacks" node
        feedbackRef = FirebaseDatabase.getInstance().getReference("feedbacks");

        btnSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFeedback();
            }
        });
    }

    private void saveFeedback() {
        String rate = etRating.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String suggestion = etSuggestions.getText().toString().trim();

        if (rate.isEmpty() || experience.isEmpty() || suggestion.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create feedback object
        Feedback feedback = new Feedback(rate, experience, suggestion);

        // Save to Firebase with unique key
        feedbackRef.push().setValue(feedback)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddFeedBack.this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                    etRating.setText("");
                    etExperience.setText("");
                    etSuggestions.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AddFeedBack.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Inner class for Feedback
    public static class Feedback {
        private String rate;
        private String experience;
        private String suggestion;


        public Feedback() {}

        public Feedback(String rate, String experience, String suggestion) {
            this.rate = rate;
            this.experience = experience;
            this.suggestion = suggestion;
        }

        // Getters
        public String getRate() { return rate; }
        public String getExperience() { return experience; }
        public String getSuggestion() { return suggestion; }
    }

    public void home(View v){
        homeIcon = findViewById(R.id.homeIcon);
        Intent intent = new Intent(AddFeedBack.this, DashBoard.class);
        startActivity(intent);
    }

    public void feedbackList(View v){
        feedbackIcon = findViewById(R.id.feedbackIcon);
        Intent intent = new Intent(AddFeedBack.this, ViewFeedback.class);
        startActivity(intent);
    }
}
