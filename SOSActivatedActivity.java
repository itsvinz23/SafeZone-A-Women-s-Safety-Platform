package com.s23010921.safezone;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SOSActivatedActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 102;

    private TextView timerText, alertStatusText, nextAlertText, alertCountRemain, sosHeading;
    private Handler handler = new Handler();

    private long startTime;
    private int alertIntervalSeconds = 180;
    private int remainingAlerts = 0;
    private long lastAlertSentTime = 0;

    private Runnable timerRunnable, nextAlertRunnable, repeatAlertRunnable;

    private boolean isActive = true;

    // Map variables
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;

    // Firebase variables
    private DatabaseReference sosEventsRef;
    private FirebaseAuth mAuth;

    // SOS Event Data - Store everything locally until deactivation
    private long sosActivatedTime;
    private String locationUrlSent = "";
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private int emergencyContactsNotified = 0;
    private int locationContactsNotified = 0;
    private boolean autoLocationEnabled = false;

    // Sound Alert Variables
    private SoundUtility soundUtility;
    private boolean isSoundAlertEnabled = false;

    // Auto-activation variables
    private boolean isAutoActivated = false;
    private String triggerType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sosactivated);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind UI
        timerText = findViewById(R.id.timerText);
        alertStatusText = findViewById(R.id.alertStatusText);
        nextAlertText = findViewById(R.id.nextAlertText);
        alertCountRemain = findViewById(R.id.alertCountRemain);
        sosHeading = findViewById(R.id.sosHeading);
        Button deactivateBtn = findViewById(R.id.deactivateButton);

        // Initialize Sound Utility
        soundUtility = new SoundUtility(this);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        sosEventsRef = FirebaseDatabase.getInstance().getReference("sos_events");

        // Check if this was auto-activated
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("auto_activated", false)) {
            isAutoActivated = true;
            triggerType = intent.getStringExtra("trigger_type");
            Log.d("SOS", "Auto-activated due to: " + triggerType);

            // Show auto-activation message
            if (triggerType != null) {
                String message = "";
                if ("impact_detected".equals(triggerType)) {
                    message = "🚨 Auto-activated: Fall Detected!";
                    sosHeading.setText("SOS - Fall Detected!");
                } else if ("button_press".equals(triggerType)) {
                    message = "🚨 Auto-activated: Emergency Button Pressed!";
                    sosHeading.setText("SOS - Button Pressed!");
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }

        // Load preferences
        loadPreferences();

        // Record SOS activation time
        startTime = System.currentTimeMillis();
        sosActivatedTime = System.currentTimeMillis();

        // Get auto-location setting
        SharedPreferences prefs = getSharedPreferences("SOS_Settings", MODE_PRIVATE);
        autoLocationEnabled = prefs.getBoolean("auto_location", false);

        // Start timers
        startTimers();

        // Setup map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapContainer);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mapContainer, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);

        // Map card click
        findViewById(R.id.locationCard).setOnClickListener(v -> {
            try {
                String query = "emergency help near me";
                Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/search/" + Uri.encode(query));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open Maps", Toast.LENGTH_SHORT).show();
            }
        });

        FrameLayout mapContainer = findViewById(R.id.mapContainer);
        mapContainer.setOnClickListener(v -> {
            try {
                String query = "emergency help near me";
                Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/search/" + Uri.encode(query));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open Maps", Toast.LENGTH_SHORT).show();
            }
        });

        // Deactivate SOS
        deactivateBtn.setOnClickListener(v -> stopSOS());

        // Check permissions and begin
        checkAndRequestPermissions();

        // Log sound alert status
        Log.d("SOSActivated", "Sound Alert Enabled: " + isSoundAlertEnabled);
        if (isSoundAlertEnabled) {
            Toast.makeText(this, "Emergency sound will play with SOS", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map settings
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Check location permission and get current location
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                getCurrentLocation();
            } else {
                // Request location permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            showLocationOnMap(location);
                        } else {
                            // If last location is not available, show a default location
                            Toast.makeText(SOSActivatedActivity.this,
                                    "Getting current location...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showLocationOnMap(Location location) {
        if (mMap == null) return;

        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("You are here")
                .snippet("Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));

        // Add emergency locations nearby
        addEmergencyLocationsNearby(currentLocation);
    }

    private void addEmergencyLocationsNearby(LatLng userLocation) {
        // Add hospitals nearby (simulated data - in real app, use Places API)
        LatLng hospital1 = new LatLng(userLocation.latitude + 0.005, userLocation.longitude + 0.005);
        LatLng hospital2 = new LatLng(userLocation.latitude - 0.003, userLocation.longitude + 0.008);
        LatLng policeStation = new LatLng(userLocation.latitude + 0.008, userLocation.longitude - 0.002);

        mMap.addMarker(new MarkerOptions()
                .position(hospital1)
                .title("Emergency Hospital")
                .snippet("24/7 Emergency Services"));

        mMap.addMarker(new MarkerOptions()
                .position(hospital2)
                .title("City Medical Center")
                .snippet("Emergency Department"));

        mMap.addMarker(new MarkerOptions()
                .position(policeStation)
                .title("Police Station")
                .snippet("Emergency: 911"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                sendFirstAlertAndSchedule();
            } else {
                Toast.makeText(this, "Permissions not granted. SOS cannot proceed.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        getCurrentLocation();
                    }
                }
            } else {
                Toast.makeText(this, "Location permission denied. Map may not show your current location.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences("SOS_Settings", MODE_PRIVATE);
        String intervalStr = prefs.getString("alert_interval", "3 min");
        String repeatStr = prefs.getString("alert_repeat", "1");

        try {
            alertIntervalSeconds = Integer.parseInt(intervalStr.split(" ")[0]) * 60;
        } catch (Exception e) {
            alertIntervalSeconds = 180;
        }

        try {
            remainingAlerts = Integer.parseInt(repeatStr);
        } catch (Exception e) {
            remainingAlerts = 1;
        }

        // Load sound alert preference
        isSoundAlertEnabled = prefs.getBoolean("sound_alert", false);
        Log.d("SOSActivated", "Sound Alert preference loaded: " + isSoundAlertEnabled);
    }

    private void startTimers() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsed / 1000);
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                timerText.setText(String.format(Locale.getDefault(),
                        "%02d:%02d:%02d", hours, minutes, secs));

                if (isActive) {
                    handler.postDelayed(this, 1000);
                }
            }
        };

        nextAlertRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isActive) return;

                if (remainingAlerts <= 0) {
                    nextAlertText.setText("0s");
                    alertCountRemain.setText("0");
                    return;
                }

                long now = System.currentTimeMillis();
                long timeSinceLast = (now - lastAlertSentTime) / 1000;
                long remaining = alertIntervalSeconds - timeSinceLast;
                if (remaining < 0) remaining = 0;

                nextAlertText.setText(remaining + "s");
                alertCountRemain.setText(String.valueOf(remainingAlerts));

                handler.postDelayed(this, 1000);
            }
        };

        repeatAlertRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isActive || remainingAlerts <= 0) return;

                remainingAlerts--;
                triggerEmergencyActions();
                lastAlertSentTime = System.currentTimeMillis();

                if (remainingAlerts > 0) {
                    handler.postDelayed(this, alertIntervalSeconds * 1000L);
                }
            }
        };

        handler.post(timerRunnable);
        handler.post(nextAlertRunnable);
    }

    private void stopSOS() {
        isActive = false;
        handler.removeCallbacks(timerRunnable);
        handler.removeCallbacks(nextAlertRunnable);
        handler.removeCallbacks(repeatAlertRunnable);

        // Stop emergency sounds
        stopEmergencySounds();

        // Save complete SOS event to Firebase
        saveCompleteSOSEvent();

        Toast.makeText(this, "SOS Deactivated", Toast.LENGTH_SHORT).show();

        // Update UI and stay on page
        timerText.setText("00:00:00");
        nextAlertText.setText("0s");
        alertCountRemain.setText("0");
        sosHeading.setText("SOS Deactivated");
    }

    private void stopEmergencySounds() {
        if (soundUtility != null) {
            soundUtility.cleanup();
            Log.d("SOSActivated", "Emergency sounds stopped");
        }
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS},
                    PERMISSION_REQUEST_CODE);
        } else {
            sendFirstAlertAndSchedule();
        }
    }

    private void sendFirstAlertAndSchedule() {
        if (remainingAlerts <= 0) {
            Toast.makeText(this, "No alerts to send. SOS will stop.", Toast.LENGTH_SHORT).show();
            stopSOS();
            return;
        }

        remainingAlerts--;
        triggerEmergencyActions();
        lastAlertSentTime = System.currentTimeMillis();
        handler.post(nextAlertRunnable);

        if (remainingAlerts > 0) {
            handler.postDelayed(repeatAlertRunnable, alertIntervalSeconds * 1000L);
        }
    }

    private void triggerEmergencyActions() {
        // Activate sound alert if enabled
        if (isSoundAlertEnabled && soundUtility != null) {
            activateEmergencySound();
        }

        DBHelper dbHelper = new DBHelper(this);
        List<ContactModel> allContacts = dbHelper.getAllContacts();

        if (allContacts.isEmpty()) {
            Toast.makeText(this, "No contacts available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debug log to verify the setting
        Log.d("SOS", "Auto location sharing enabled: " + autoLocationEnabled);
        Log.d("SOS", "Sound alert enabled: " + isSoundAlertEnabled);
        Toast.makeText(this, "Auto location: " + autoLocationEnabled + ", Sound: " + isSoundAlertEnabled, Toast.LENGTH_SHORT).show();

        Collections.sort(allContacts, Comparator.comparingInt(c -> Integer.parseInt(c.getPriority())));
        int count = Math.min(5, allContacts.size());

        StringBuilder statusBuilder = new StringBuilder();

        // Add sound activation status to UI
        if (isSoundAlertEnabled) {
            statusBuilder.append("🔊 Emergency Sound Activated\n");
        }

        // Add auto-activation info if applicable
        if (isAutoActivated) {
            statusBuilder.append("🚨 Auto-activated: ").append(triggerType).append("\n");
        }

        // Send SOS EMERGENCY ALERT messages first
        emergencyContactsNotified = 0;
        for (int i = 0; i < count; i++) {
            ContactModel contact = allContacts.get(i);
            String name = contact.getFname() + " " + contact.getLname();
            String number = contact.getNumber().trim();

            // Validate phone number
            if (number.isEmpty() || !android.util.Patterns.PHONE.matcher(number).matches()) {
                continue;
            }

            // Send SOS message
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                String emergencyMessage = "🚨 EMERGENCY ALERT! This is " + name +
                        " - I'm in danger and need immediate help! Please contact me or alert authorities.";

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    ArrayList<String> parts = smsManager.divideMessage(emergencyMessage);
                    smsManager.sendMultipartTextMessage(number, null, parts, null, null);

                    emergencyContactsNotified++;
                    statusBuilder.append("✅ ").append(name).append(" - SOS Alert Sent\n");
                    Log.d("SOS", "Emergency SMS sent to: " + name + " (" + number + ")");
                } catch (Exception e) {
                    Log.e("SOS", "Failed to send emergency SMS to " + number, e);
                    statusBuilder.append("❌ ").append(name).append(" - SMS Failed\n");
                }
            }

            // Make the first call
            if (i == 0 && ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                    startActivity(intent);
                    statusBuilder.append("📞 ").append(name).append(" - Calling...\n");
                } catch (Exception e) {
                    Log.e("SOS", "Failed to call " + number, e);
                    statusBuilder.append("❌ ").append(name).append(" - Call Failed\n");
                }
            }
        }

        alertStatusText.setText(statusBuilder.toString());
        Toast.makeText(this, "SOS alert sent to " + emergencyContactsNotified + " contacts", Toast.LENGTH_SHORT).show();

        // ✅ If auto location is ON, automatically send location as SEPARATE message
        if (autoLocationEnabled) {
            Log.d("SOS", "Auto location enabled - sending location as separate message");
            sendLocationToContactsSeparately();
        } else {
            Log.d("SOS", "Auto location disabled - only emergency alert sent");
        }
    }

    private void activateEmergencySound() {
        if (soundUtility != null) {
            // Play emergency sound
            soundUtility.playEmergencySound();

            // Also speak an emergency message
            String emergencyMessage = "Emergency! SOS activated! Help is on the way!";
            soundUtility.speakEmergencyMessage(emergencyMessage);

            Log.d("SOS", "Emergency sound and voice activated");
        } else {
            Log.e("SOS", "SoundUtility is null - cannot activate emergency sound");
        }
    }

    private void sendLocationToContactsSeparately() {
        Log.d("SOS", "Starting location sharing as separate message...");

        // Check permissions first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SOS", "Location permission missing");
            Toast.makeText(this, "Location permission required for auto-sharing.", Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SOS", "SMS permission missing");
            Toast.makeText(this, "SMS permission required for auto-sharing.", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if device can send SMS
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            Toast.makeText(this, "Device cannot send SMS", Toast.LENGTH_SHORT).show();
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Log.d("SOS", "Location obtained: " + location.getLatitude() + ", " + location.getLongitude());

                double lat = location.getLatitude();
                double lng = location.getLongitude();
                String locationUrl = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng;

                // Store location data for Firebase
                locationUrlSent = locationUrl;
                currentLatitude = lat;
                currentLongitude = lng;

                String locationMessage = "📍 EMERGENCY LOCATION:\n" +
                        "Google Maps: " + locationUrl +
                        "\nCoordinates: " + lat + ", " + lng +
                        "\nSent via SafeZone SOS";

                DBHelper dbHelper = new DBHelper(this);
                List<ContactModel> allContacts = dbHelper.getAllContacts();

                if (allContacts.isEmpty()) {
                    Toast.makeText(this, "No contacts to send location to.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Collections.sort(allContacts, Comparator.comparingInt(c -> Integer.parseInt(c.getPriority())));
                int count = Math.min(5, allContacts.size());
                SmsManager smsManager = SmsManager.getDefault();

                // Use final array to work around lambda restriction
                final int[] locationSmsSent = {0};
                for (int i = 0; i < count; i++) {
                    ContactModel contact = allContacts.get(i);
                    String number = contact.getNumber().trim();
                    String name = contact.getFname() + " " + contact.getLname();

                    // Validate phone number
                    if (number.isEmpty() || !android.util.Patterns.PHONE.matcher(number).matches()) {
                        continue;
                    }

                    try {
                        // Split long messages and send as multipart
                        ArrayList<String> parts = smsManager.divideMessage(locationMessage);
                        smsManager.sendMultipartTextMessage(number, null, parts, null, null);

                        locationSmsSent[0]++;
                        Log.d("SOS", "Location SMS sent to: " + name + " (" + number + ")");
                    } catch (Exception e) {
                        Log.e("SOS", "Failed to send location to " + number, e);
                    }
                }

                // Store location contacts count
                locationContactsNotified = locationSmsSent[0];

                // Update UI on main thread
                runOnUiThread(() -> {
                    String currentStatus = alertStatusText.getText().toString();
                    alertStatusText.setText(currentStatus + "\n📍 Location sent to " + locationContactsNotified + " contacts");
                    Toast.makeText(SOSActivatedActivity.this,
                            "Emergency location sent to " + locationContactsNotified + " contacts",
                            Toast.LENGTH_LONG).show();
                });

            } else {
                Log.e("SOS", "Last location is null - no location available");
                // Last location is null, try to get current location
                getCurrentLocationForSharing();
            }
        }).addOnFailureListener(e -> {
            Log.e("SOS", "Failed to get location: " + e.getMessage());
            Toast.makeText(SOSActivatedActivity.this,
                    "Failed to get location: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void getCurrentLocationForSharing() {
        Log.d("SOS", "Attempting to get fresh location...");
        Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SOS", "No location permission for fresh location");
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Log.d("SOS", "Fresh location obtained: " + location.getLatitude() + ", " + location.getLongitude());
                // Retry sending with the new location
                sendLocationToContactsSeparately();
            } else {
                Log.e("SOS", "Still no location available");
                Toast.makeText(SOSActivatedActivity.this,
                        "Location unavailable. Please check GPS.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveCompleteSOSEvent() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("SOS", "User not logged in");
            Toast.makeText(this, "User not logged in. SOS event not saved.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate unique event ID
        String sosEventId = sosEventsRef.push().getKey();
        if (sosEventId == null) {
            Log.e("SOS", "Failed to generate event ID");
            return;
        }

        // Calculate duration
        long deactivatedTime = System.currentTimeMillis();
        long durationSeconds = (deactivatedTime - sosActivatedTime) / 1000;

        // Format times
        String activatedTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(sosActivatedTime));
        String deactivatedTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(deactivatedTime));

        // Get user info
        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "unknown@email.com";

        // Calculate total contacts notified
        int totalContactsNotified = emergencyContactsNotified + locationContactsNotified;

        // Create complete SOS event data
        Map<String, Object> sosEvent = new HashMap<>();
        sosEvent.put("user_id", userId);
        sosEvent.put("userEmail", userEmail);
        sosEvent.put("sosActivatedTime", activatedTimeStr);
        sosEvent.put("sosDeactivatedTime", deactivatedTimeStr);
        sosEvent.put("locationUrl", locationUrlSent);
        sosEvent.put("autolocationEnabled", autoLocationEnabled);
        sosEvent.put("soundAlertEnabled", isSoundAlertEnabled);
        sosEvent.put("contactsNotified", totalContactsNotified);
        sosEvent.put("emergencyContactsNotified", emergencyContactsNotified);
        sosEvent.put("locationContactsNotified", locationContactsNotified);
        sosEvent.put("durationSeconds", durationSeconds);

        // Add auto-activation info
        sosEvent.put("auto_activated", isAutoActivated);
        if (isAutoActivated) {
            sosEvent.put("trigger_type", triggerType);
        }

        // Coordinates
        Map<String, Object> coordinates = new HashMap<>();
        coordinates.put("latitude", currentLatitude);
        coordinates.put("longitude", currentLongitude);
        sosEvent.put("coordinates", coordinates);

        // Debug log
        Log.d("SOS", "Saving complete SOS event to: sos_events/" + sosEventId);
        Log.d("SOS", "Duration: " + durationSeconds + " seconds, Contacts: " + totalContactsNotified + ", Sound: " + isSoundAlertEnabled + ", Auto: " + isAutoActivated);

        // Save complete event to Firebase
        sosEventsRef.child(sosEventId).setValue(sosEvent)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SOS", "Complete SOS event saved to Firebase successfully");
                    runOnUiThread(() ->
                            Toast.makeText(SOSActivatedActivity.this, "SOS event saved to cloud", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    Log.e("SOS", "Failed to save SOS event: " + e.getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(SOSActivatedActivity.this, "Failed to save SOS event", Toast.LENGTH_SHORT).show());
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
        handler.removeCallbacks(nextAlertRunnable);
        handler.removeCallbacks(repeatAlertRunnable);

        // Clean up sound resources
        if (soundUtility != null) {
            soundUtility.cleanup();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // You can choose to stop sounds when app goes to background
        // or keep them playing depending on your requirements
        // stopEmergencySounds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check sound preference when resuming
        if (soundUtility != null) {
            isSoundAlertEnabled = soundUtility.isSoundAlertEnabled(this);
        }
    }

    public void setViewMoreContacts(View v) {
        Intent intent = new Intent(SOSActivatedActivity.this, ContactList.class);
        startActivity(intent);
    }

    public void setViewMoreLocation(View v) {
        Intent intent = new Intent(SOSActivatedActivity.this, SafePlace.class);
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

    public void setShareLocation(View v) {
        NavigationHelper.goToShareLocation(this);
    }
}
