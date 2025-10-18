package com.s23010921.safezone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Map;

public class SosSettings extends AppCompatActivity {

    Switch switchVoiceTrigger, switchSoundAlert, switchAutoLocation;
    EditText etCustomMessage;
    Spinner spinnerAlertRepeat, spinnerAlertInterval;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sos_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Link UI elements
        switchVoiceTrigger = findViewById(R.id.switchVoiceTrigger);
        switchSoundAlert = findViewById(R.id.switchSoundAlert);
        switchAutoLocation = findViewById(R.id.switchAutoLocation);
        etCustomMessage = findViewById(R.id.etCustomMessage);
        spinnerAlertRepeat = findViewById(R.id.spinnerAlertRepeat);
        spinnerAlertInterval = findViewById(R.id.spinnerAlertInterval);
        btnSave = findViewById(R.id.btnSave);

        // Load previously saved preferences
        loadPreferences();
        printSharedPreferences();
    }

    public void setDashboard(View v) {
        savePreferences(); // Save settings before moving to dashboard
        Intent intent = new Intent(SosSettings.this, DashBoard.class);
        startActivity(intent);
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }

    private void savePreferences() {
        SharedPreferences preferences = getSharedPreferences("SOS_Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("voice_trigger", switchVoiceTrigger.isChecked());
        editor.putBoolean("sound_alert", switchSoundAlert.isChecked());
        editor.putBoolean("auto_location", switchAutoLocation.isChecked());
        editor.putString("custom_message", etCustomMessage.getText().toString());
        editor.putString("alert_repeat", spinnerAlertRepeat.getSelectedItem().toString());
        editor.putString("alert_interval", spinnerAlertInterval.getSelectedItem().toString());

        editor.apply(); // Save changes
    }

    private void loadPreferences() {
        SharedPreferences preferences = getSharedPreferences("SOS_Settings", MODE_PRIVATE);

        switchVoiceTrigger.setChecked(preferences.getBoolean("voice_trigger", false));
        switchSoundAlert.setChecked(preferences.getBoolean("sound_alert", false));
        switchAutoLocation.setChecked(preferences.getBoolean("auto_location", false));
        etCustomMessage.setText(preferences.getString("custom_message", ""));

        String savedRepeat = preferences.getString("alert_repeat", "");
        String savedInterval = preferences.getString("alert_interval", "");

        if (spinnerAlertRepeat.getAdapter() != null) {
            int posRepeat = ((android.widget.ArrayAdapter<String>) spinnerAlertRepeat.getAdapter()).getPosition(savedRepeat);
            spinnerAlertRepeat.setSelection(posRepeat);
        }

        if (spinnerAlertInterval.getAdapter() != null) {
            int posInterval = ((android.widget.ArrayAdapter<String>) spinnerAlertInterval.getAdapter()).getPosition(savedInterval);
            spinnerAlertInterval.setSelection(posInterval);
        }
    }
    private void printSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("SOS_Settings", MODE_PRIVATE);
        Map<String, ?> allPrefs = prefs.getAll();

        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            Log.d("SharedPrefDebug", entry.getKey() + " = " + String.valueOf(entry.getValue()));
        }

        // Optional: Also show in a Toast for quick view
        String interval = prefs.getString("alert_interval", "N/A");
        String repeat = prefs.getString("alert_repeat", "N/A");
        Toast.makeText(this, "Interval: " + interval + ", Repeat: " + repeat, Toast.LENGTH_LONG).show();
    }

}
