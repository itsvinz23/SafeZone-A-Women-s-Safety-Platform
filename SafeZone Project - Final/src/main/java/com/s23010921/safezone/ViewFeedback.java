package com.s23010921.safezone;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewFeedback extends AppCompatActivity {

    private LinearLayout feedbackContainer;
    private TextView tvFeedbackCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_feedback);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        feedbackContainer = findViewById(R.id.feedbackContainer);
        tvFeedbackCount = findViewById(R.id.tvFeedbackCount);

        // Fetch latest feedbacks from Firebase
        fetchFeedbacks();
    }

    private void fetchFeedbacks() {
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("feedbacks");

        // Query last 10 feedbacks (newest last in Firebase push keys)
        Query lastTenQuery = feedbackRef.orderByKey().limitToLast(10);

        lastTenQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                feedbackContainer.removeAllViews(); // Clear previous cards

                List<DataSnapshot> feedbackList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    feedbackList.add(data);
                }

                // Reverse list to show newest feedback first
                Collections.reverse(feedbackList);

                int feedbackCount = 0;
                for (DataSnapshot feedback : feedbackList) {
                    String rate = feedback.child("rate").getValue(String.class);
                    String experience = feedback.child("experience").getValue(String.class);
                    String suggestion = feedback.child("suggestion").getValue(String.class);

                    if (addFeedbackCard(rate, experience, suggestion)) {
                        feedbackCount++;
                    }
                }

                // Update feedback count
                if (feedbackCount == 0) {
                    tvFeedbackCount.setText("No feedbacks yet");
                } else {
                    tvFeedbackCount.setText(feedbackCount + " feedback" + (feedbackCount > 1 ? "s" : ""));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ViewFeedback.this, "Failed to load feedbacks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean addFeedbackCard(String rate, String experience, String suggestion) {
        // Check if all fields are empty or null
        if ((rate == null || rate.isEmpty()) &&
                (experience == null || experience.isEmpty()) &&
                (suggestion == null || suggestion.isEmpty())) {
            return false;
        }

        // Inflate card layout
        View card = LayoutInflater.from(this).inflate(R.layout.feedback_card_item, feedbackContainer, false);

        // Find all the views in the card
        TextView ratingText = card.findViewById(R.id.ratingText);
        TextView experienceText = card.findViewById(R.id.experienceText);
        TextView suggestionText = card.findViewById(R.id.suggestionText);
        LinearLayout starContainer = card.findViewById(R.id.starContainer);

        // Set rating text and stars
        if (rate != null && !rate.isEmpty()) {
            ratingText.setText(rate + "/5");

            // Add star rating visualization
            try {
                int ratingValue = Integer.parseInt(rate);
                addStarsToContainer(starContainer, ratingValue);
            } catch (NumberFormatException e) {
                ratingText.setText("N/A");
                // Clear stars if rating is invalid
                starContainer.removeAllViews();
            }
        } else {
            ratingText.setText("N/A");
            // Clear stars if no rating
            starContainer.removeAllViews();
        }

        // Set experience text
        if (experience != null && !experience.isEmpty()) {
            experienceText.setText(experience);
        } else {
            experienceText.setText("No experience provided");
        }

        // Set suggestion text
        if (suggestion != null && !suggestion.isEmpty()) {
            suggestionText.setText(suggestion);
        } else {
            suggestionText.setText("No suggestions provided");
        }

        feedbackContainer.addView(card);
        return true;
    }

    private void addStarsToContainer(LinearLayout container, int rating) {
        container.removeAllViews(); // Clear existing stars

        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
            if (i > 1) params.setMarginStart(4);
            star.setLayoutParams(params);

            if (i <= rating) {
                star.setImageResource(R.drawable.ic_star_filled);
            } else {
                star.setImageResource(R.drawable.ic_star_outline);
            }
            container.addView(star);
        }
    }

    public void setDashboard(View v){
        ImageView homeIcon = findViewById(R.id.homeIcon);
        Intent intent = new Intent(ViewFeedback.this, DashBoard.class);
        startActivity(intent);
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