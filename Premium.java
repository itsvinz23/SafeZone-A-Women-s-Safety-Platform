package com.s23010921.safezone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Premium extends AppCompatActivity {

    private TextView tvFallStatus, tvGyroData, tvButtonStatus, tvConnectionStatus;
    private Button btnRefresh;

    private DatabaseReference alertsRef, deviceStatusRef;
    private String currentDeviceId = "F4:CF:A2:F5:0E:3A";

    // Add Firebase Auth
    private FirebaseAuth mAuth;

    // Track the latest alert to avoid duplicate SOS activations
    private String lastProcessedAlertKey = "";
    private long lastProcessedTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_premium);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If not signed in, show message and return
            Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d("Premium", "User authenticated: " + currentUser.getEmail());

        // Initialize UI components
        initializeUI();

        // Initialize Firebase Database
        initializeFirebase();

        // Start listening for alerts
        startAlertMonitoring();

        // Load initial device status
        loadDeviceStatus();
    }

    private void initializeUI() {
        tvFallStatus = findViewById(R.id.tvFallStatus);
        tvGyroData = findViewById(R.id.tvGyroData);
        tvButtonStatus = findViewById(R.id.tvButtonStatus);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnRefresh.setOnClickListener(v -> refreshSensorData());
    }

    private void initializeFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        alertsRef = database.getReference("alerts");
        deviceStatusRef = database.getReference("device_status");

        // Test connection
        testFirebaseConnection();
    }

    private void testFirebaseConnection() {
        alertsRef.limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Premium", "Firebase connection successful");
                Toast.makeText(Premium.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Premium", "Firebase connection failed: " + databaseError.getMessage());
                Log.e("Premium", "Error code: " + databaseError.getCode());
                Log.e("Premium", "Error details: " + databaseError.getDetails());

                runOnUiThread(() -> {
                    Toast.makeText(Premium.this,
                            "Firebase error: " + databaseError.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // Show more specific error message
                    if (databaseError.getCode() == DatabaseError.PERMISSION_DENIED) {
                        Toast.makeText(Premium.this,
                                "Permission denied - Check Firebase security rules",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void startAlertMonitoring() {
        alertsRef.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("Premium", "New alert data received");
                    for (DataSnapshot alertSnapshot : dataSnapshot.getChildren()) {
                        processNewAlert(alertSnapshot);
                    }
                } else {
                    Log.d("Premium", "No alerts found in database");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Premium", "Error reading alerts: " + databaseError.getMessage());
                Log.e("Premium", "Error details: " + databaseError.getDetails());

                runOnUiThread(() -> {
                    Toast.makeText(Premium.this,
                            "Alert monitoring error: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void processNewAlert(DataSnapshot alertSnapshot) {
        try {
            String alertKey = alertSnapshot.getKey();
            Map<String, Object> alertData = (Map<String, Object>) alertSnapshot.getValue();

            if (alertData == null) {
                Log.e("Premium", "Alert data is null");
                return;
            }

            String deviceId = (String) alertData.get("device_id");
            String status = (String) alertData.get("status");
            String trigger = (String) alertData.get("trigger");
            String type = (String) alertData.get("type");

            // Debug log all fields
            Log.d("Premium", "Processing alert - Device: " + deviceId +
                    ", Status: " + status + ", Trigger: " + trigger + ", Type: " + type);

            // FIX: Handle both String and Long timestamps
            Long timestamp = null;
            Object timestampObj = alertData.get("timestamp");
            if (timestampObj instanceof Long) {
                timestamp = (Long) timestampObj;
            } else if (timestampObj instanceof String) {
                try {
                    timestamp = Long.parseLong((String) timestampObj);
                } catch (NumberFormatException e) {
                    Log.e("Premium", "Invalid timestamp format: " + timestampObj);
                    return;
                }
            } else if (timestampObj instanceof Integer) {
                timestamp = ((Integer) timestampObj).longValue();
            }

            if (timestamp == null) {
                Log.e("Premium", "Timestamp is null or invalid");
                return;
            }

            // Check if this is a new alert for our device
            if (deviceId != null && deviceId.equals(currentDeviceId) && timestamp > lastProcessedTimestamp) {

                lastProcessedAlertKey = alertKey;
                lastProcessedTimestamp = timestamp;

                Log.d("Premium", "New alert processed: " + trigger + " - " + type + " - TS: " + timestamp);

                // Update sensor display based on trigger type
                updateSensorDisplay(trigger, status);

                // If status is "active", automatically activate SOS
                if ("active".equals(status) && "SOS".equals(type)) {
                    activateSOSBasedOnTrigger(trigger);
                }
            }
        } catch (Exception e) {
            Log.e("Premium", "Error processing alert: " + e.getMessage());
            e.printStackTrace();

            runOnUiThread(() -> {
                Toast.makeText(Premium.this,
                        "Error processing alert: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateSensorDisplay(String trigger, String status) {
        runOnUiThread(() -> {
            // Update connection status
            if ("active".equals(status)) {
                tvConnectionStatus.setText("Connected");
                tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvConnectionStatus.setText("Disconnected");
                tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            // Update other sensors based on trigger type
            switch (trigger) {
                case "impact_detected":
                    tvFallStatus.setText("Fall Detected!");
                    tvFallStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    tvGyroData.setText("Unstable");
                    tvGyroData.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    tvButtonStatus.setText("OFF");
                    tvButtonStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;

                case "button_press":
                    tvFallStatus.setText("No Fall");
                    tvFallStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    tvGyroData.setText("Stable");
                    tvGyroData.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    tvButtonStatus.setText("ON - Emergency!");
                    tvButtonStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    break;

                default:
                    tvFallStatus.setText("No Fall");
                    tvFallStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    tvGyroData.setText("Stable");
                    tvGyroData.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    tvButtonStatus.setText("OFF");
                    tvButtonStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
            }

            Toast.makeText(Premium.this,
                    "Alert: " + trigger + " - Status: " + status,
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void activateSOSBasedOnTrigger(String trigger) {
        Log.d("Premium", "Activating SOS due to: " + trigger);

        // Start SOS Activated Activity
        Intent sosIntent = new Intent(Premium.this, SOSActivatedActivity.class);

        // Pass trigger information to SOS activity
        sosIntent.putExtra("trigger_type", trigger);
        sosIntent.putExtra("auto_activated", true);

        startActivity(sosIntent);

        // Show notification
        runOnUiThread(() -> {
            String message = "";
            if ("impact_detected".equals(trigger)) {
                message = "🚨 Fall detected! Activating SOS automatically!";
            } else if ("button_press".equals(trigger)) {
                message = "🚨 Emergency button pressed! Activating SOS!";
            }

            Toast.makeText(Premium.this, message, Toast.LENGTH_LONG).show();
        });
    }

    private void loadDeviceStatus() {
        deviceStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> statusData = (Map<String, Object>) dataSnapshot.getValue();
                    if (statusData != null) {
                        String deviceStatus = (String) statusData.get("status");
                        runOnUiThread(() -> {
                            if ("online".equals(deviceStatus)) {
                                tvConnectionStatus.setText("Connected");
                                tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            } else {
                                tvConnectionStatus.setText("Disconnected");
                                tvConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Premium", "Error reading device status: " + databaseError.getMessage());
                Log.e("Premium", "Error details: " + databaseError.getDetails());

                runOnUiThread(() -> {
                    Toast.makeText(Premium.this,
                            "Device status error: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void refreshSensorData() {
        // Re-check the latest alert
        alertsRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot alertSnapshot : dataSnapshot.getChildren()) {
                        processNewAlert(alertSnapshot);
                    }
                    Toast.makeText(Premium.this, "Sensor data refreshed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Premium.this, "No alerts found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Premium", "Failed to refresh data: " + databaseError.getMessage());
                Log.e("Premium", "Error details: " + databaseError.getDetails());
                Log.e("Premium", "Error code: " + databaseError.getCode());

                runOnUiThread(() -> {
                    String errorMessage = "Failed to refresh: " + databaseError.getMessage();

                    // Provide more specific error messages
                    if (databaseError.getCode() == DatabaseError.PERMISSION_DENIED) {
                        errorMessage = "Permission Denied - Check Firebase security rules";
                    } else if (databaseError.getCode() == DatabaseError.DISCONNECTED) {
                        errorMessage = "Network disconnected - Check internet connection";
                    }

                    Toast.makeText(Premium.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up listeners if needed
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

    public void setShareLocation(View v) {
        NavigationHelper.goToShareLocation(this);
    }
}