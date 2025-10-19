package com.s23010921.safezone;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

import com.google.android.gms.maps.SupportMapFragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SOSActivatedActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;

    private TextView timerText, alertStatusText, nextAlertText, alertCountRemain, sosHeading;
    private Handler handler = new Handler();

    private long startTime;
    private int alertIntervalSeconds = 180;
    private int remainingAlerts = 0;
    private long lastAlertSentTime = 0;

    private Runnable timerRunnable, nextAlertRunnable, repeatAlertRunnable;

    private boolean isActive = true;

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

        // Load preferences
        loadPreferences();

        // Start timers
        startTime = System.currentTimeMillis();
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
                        "SOS activated: %02d:%02d:%02d", hours, minutes, secs));

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
                    nextAlertText.setText("Next alert in: 0s");
                    alertCountRemain.setText("Remaining Alerts: 0");
                    return;
                }

                long now = System.currentTimeMillis();
                long timeSinceLast = (now - lastAlertSentTime) / 1000;
                long remaining = alertIntervalSeconds - timeSinceLast;
                if (remaining < 0) remaining = 0;

                nextAlertText.setText("Next alert in: " + remaining + "s");
                alertCountRemain.setText("Remaining Alerts: " + remainingAlerts);

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
    }

    private void stopSOS() {
        isActive = false;
        handler.removeCallbacks(timerRunnable);
        handler.removeCallbacks(nextAlertRunnable);
        handler.removeCallbacks(repeatAlertRunnable);

        Toast.makeText(this, "SOS Deactivated", Toast.LENGTH_SHORT).show();

        // Update UI and stay on page
        timerText.setText("SOS Deactivated");
        nextAlertText.setText("Next alert in: 0s");
        alertCountRemain.setText("Remaining Alerts: 0");
        sosHeading.setText("SOS Deactivated");
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
        }
    }

    private void triggerEmergencyActions() {
        DBHelper dbHelper = new DBHelper(this);
        List<ContactModel> allContacts = dbHelper.getAllContacts();

        if (allContacts.isEmpty()) {
            Toast.makeText(this, "No contacts available.", Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.sort(allContacts, Comparator.comparingInt(c -> Integer.parseInt(c.getPriority())));
        int count = Math.min(5, allContacts.size());

        StringBuilder statusBuilder = new StringBuilder();

        for (int i = 0; i < count; i++) {
            ContactModel contact = allContacts.get(i);
            String name = contact.getFname() + " " + contact.getLname();
            String number = contact.getNumber();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                SmsManager.getDefault().sendTextMessage(number, null,
                        "🚨 Emergency! Is this " + name + "? I need your help!", null, null);
                statusBuilder.append(name).append(" - ").append(" (SMS Sent)\n");
            }

            if (i == 0 && ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                startActivity(intent);
                statusBuilder.append(name).append(" - ").append(" (Calling...)\n");
            }
        }

        alertStatusText.setText(statusBuilder.toString());
        Toast.makeText(this, "SOS alert sent.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
        handler.removeCallbacks(nextAlertRunnable);
        handler.removeCallbacks(repeatAlertRunnable);
    }

    public void setViewMoreContacts(View v) {
        Intent intent = new Intent(SOSActivatedActivity.this, ContactList.class);
        startActivity(intent);
    }

    public void setViewMoreLocation(View v) {
        Intent intent = new Intent(SOSActivatedActivity.this, SafePlace.class);
        startActivity(intent);
    }

    public void setHomeIcon(View v) {
        Intent intent = new Intent(SOSActivatedActivity.this, DashBoard.class);
        startActivity(intent);
    }
}
