package com.s23010921.safezone;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileUpdate extends AppCompatActivity {

    private EditText etContact, etEmail, etNewPassword, etConfirmPassword;
    private Button btnUpdate;
    private TextView tvGreeting;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Bind UI
        tvGreeting = findViewById(R.id.tvGreeting);
        etContact = findViewById(R.id.etContact);
        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnUpdate = findViewById(R.id.btnUpdate);

        // Load user details
        loadUserDetails();

        // Update button click
        btnUpdate.setOnClickListener(v -> updateProfile());
    }

    private void loadUserDetails() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String contact = snapshot.child("contact").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    // Set greeting
                    if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
                        tvGreeting.setText("Hey " + firstName + " " + lastName + " 👋");
                    }

                    // Fill fields
                    if (!TextUtils.isEmpty(contact)) {
                        etContact.setText(contact);
                    }
                    if (!TextUtils.isEmpty(email)) {
                        etEmail.setText(email);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileUpdate.this, "Failed to load details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        if (currentUser == null) return;

        String newContact = etContact.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newContact) && TextUtils.isEmpty(newEmail)
                && TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Nothing to update", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(newPassword)) {
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String uid = currentUser.getUid();
        Map<String, Object> updates = new HashMap<>();

        if (!TextUtils.isEmpty(newContact)) {
            updates.put("contact", newContact);
        }

        if (!TextUtils.isEmpty(newEmail)) {
            currentUser.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> {
                        updates.put("email", newEmail);
                        saveToDatabase(uid, updates);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ProfileUpdate.this, "Email update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }

        if (!TextUtils.isEmpty(newPassword)) {
            currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileUpdate.this, "Password updated. Please log in again.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        // Redirect to LoginActivity
                        startActivity(new Intent(ProfileUpdate.this, Login.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ProfileUpdate.this, "Password update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }

        if (!updates.isEmpty() && TextUtils.isEmpty(newEmail)) {
            saveToDatabase(uid, updates);
        }
    }

    private void saveToDatabase(String uid, Map<String, Object> updates) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(ProfileUpdate.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(ProfileUpdate.this, "Database update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
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
}