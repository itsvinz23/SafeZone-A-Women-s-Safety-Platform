package com.s23010921.safezone;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashBoard extends AppCompatActivity {

    // Cards and UI components
    CardView safePlacesCard, settingsCard, contactsCard, shareLocationCard;
    ImageView imgSafePlaces, imgSOSSettings, imgContacts;
    TextView txtSafePlaces, txtSOSSettings, txtContacts, tvFirstName;
    MaterialButton sosButton, btnProfile, shareExperienceBtn;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash_board);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind UI
        tvFirstName = findViewById(R.id.tvFirstName);

        sosButton = findViewById(R.id.sosButton);
        btnProfile = findViewById(R.id.btnProfile);
        shareExperienceBtn = findViewById(R.id.shareExperienceBtn);

        safePlacesCard = findViewById(R.id.safePlacesCard);
        settingsCard = findViewById(R.id.settingsCard);
        contactsCard = findViewById(R.id.contactsCard);
        shareLocationCard = findViewById(R.id.shareLocationCard);

        imgSafePlaces = findViewById(R.id.imgSafePlaces);
        imgSOSSettings = findViewById(R.id.imgSOSSettings);
        imgContacts = findViewById(R.id.imgContacts);

        txtSafePlaces = findViewById(R.id.txtSafePlaces);
        txtSOSSettings = findViewById(R.id.txtSOSSettings);
        txtContacts = findViewById(R.id.txtContacts);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            loadUserName();
        }
    }

    private void loadUserName() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    if (!TextUtils.isEmpty(firstName)) {
                        tvFirstName.setText(firstName);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DashBoard.this, "Failed to load name: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setSafePlace(View v) {
        Intent intent = new Intent(DashBoard.this, SafePlace.class);
        startActivity(intent);
    }

    public void setSOSSettings(View v) {
        Intent intent = new Intent(DashBoard.this, SosSettings.class);
        startActivity(intent);
    }

    public void setContactsCard(View v) {
        Intent intent = new Intent(DashBoard.this, ContactList.class);
        startActivity(intent);
    }

    public void setSosButton(View v) {
        Intent intent = new Intent(DashBoard.this, SOSActivatedActivity.class);
        startActivity(intent);
    }

    public void setBtnProfile(View v) {
        Intent intent = new Intent(DashBoard.this, ProfileUpdate.class);
        startActivity(intent);
    }

    public void setFeedback(View v) {
        Intent intent = new Intent(DashBoard.this, AddFeedBack.class);
        startActivity(intent);
    }

    public void setShareLocationCard(View v) {
        Intent intent = new Intent(DashBoard.this, ShareLocation.class);
        startActivity(intent);
    }
}
