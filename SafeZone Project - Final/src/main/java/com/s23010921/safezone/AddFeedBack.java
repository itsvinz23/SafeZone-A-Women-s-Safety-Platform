package com.s23010921.safezone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddFeedBack extends AppCompatActivity {

    private EditText etExperience, etSuggestions;
    private Button btnSendFeedback;
    private ImageView homeIcon, feedbackIcon;
    private DatabaseReference feedbackRef;
    private LinearLayout starRatingLayout;
    private TextView tvRatingText;
    private int selectedRating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_feed_back);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI
        etExperience = findViewById(R.id.etExperience);
        etSuggestions = findViewById(R.id.etSuggestions);
        btnSendFeedback = findViewById(R.id.btnSendFeedback);
        starRatingLayout = findViewById(R.id.starRatingLayout);
        tvRatingText = findViewById(R.id.tvRatingText);

        // Firebase reference for "feedbacks" node
        feedbackRef = FirebaseDatabase.getInstance().getReference("feedbacks");

        // Setup star rating
        setupStarRating();

        btnSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFeedback();
            }
        });
    }

    private void setupStarRating() {
        // Add 5 stars
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(48, 48);
            if (i > 1) params.setMarginStart(8);
            star.setLayoutParams(params);
            star.setImageResource(R.drawable.ic_star_outline);
            star.setTag(i);
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int rating = (int) v.getTag();
                    setRating(rating);
                }
            });
            starRatingLayout.addView(star);
        }
    }

    private void setRating(int rating) {
        selectedRating = rating;

        // Update rating text based on selection
        String[] ratingTexts = {"Tap stars to rate", "Poor", "Fair", "Good", "Great", "Excellent"};
        tvRatingText.setText(ratingTexts[rating]);

        // Update star appearances
        for (int i = 0; i < starRatingLayout.getChildCount(); i++) {
            ImageView star = (ImageView) starRatingLayout.getChildAt(i);
            int starPosition = (int) star.getTag();
            if (starPosition <= rating) {
                star.setImageResource(R.drawable.ic_star_filled);
            } else {
                star.setImageResource(R.drawable.ic_star_outline);
            }
        }
    }

    private void saveFeedback() {
        String experience = etExperience.getText().toString().trim();
        String suggestion = etSuggestions.getText().toString().trim();

        if (selectedRating == 0) {
            Toast.makeText(this, "Please rate your experience", Toast.LENGTH_SHORT).show();
            return;
        }

        if (experience.isEmpty() || suggestion.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create feedback object with star rating
        Feedback feedback = new Feedback(String.valueOf(selectedRating), experience, suggestion);

        // Save to Firebase with unique key
        feedbackRef.push().setValue(feedback)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddFeedBack.this, "Feedback submitted successfully! 🌟", Toast.LENGTH_SHORT).show();
                    etExperience.setText("");
                    etSuggestions.setText("");
                    // Reset stars
                    setRating(0);
                    tvRatingText.setText("Tap stars to rate");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AddFeedBack.this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
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

    public void setSosButton(View v) {
        NavigationHelper.goToSOS(this);
    }

    public void setHome(View v) {
        NavigationHelper.goToHome(this);
    }

    public void setLogout(View v) {
        NavigationHelper.goToLogout(this);
    }
    public void setProfile(View v) {
        NavigationHelper.goToProfile(this);
    }
    public void setSafePlaces(View v) {
        NavigationHelper.goToSafePlaces(this);
    }
    public void setAddFeedback(View v) {
        NavigationHelper.goToAddFeedback(this);
    }
    public void setViewFeedback(View v) {
        NavigationHelper.goToFeedbackList(this);
    }
}