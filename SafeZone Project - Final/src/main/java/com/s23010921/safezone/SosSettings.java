package com.s23010921.safezone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.Map;

public class SosSettings extends AppCompatActivity {

    // FIX: Change from Switch to SwitchMaterial
    SwitchMaterial switchVoiceTrigger, switchSoundAlert, switchAutoLocation;
    EditText etCustomMessage;
    MaterialAutoCompleteTextView spinnerAlertRepeat, spinnerAlertInterval;
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

        // Link UI elements - these will now work correctly
        switchVoiceTrigger = findViewById(R.id.switchVoiceTrigger);
        switchSoundAlert = findViewById(R.id.switchSoundAlert);
        switchAutoLocation = findViewById(R.id.switchAutoLocation);
        etCustomMessage = findViewById(R.id.etCustomMessage);
        spinnerAlertRepeat = findViewById(R.id.spinnerAlertRepeat);
        spinnerAlertInterval = findViewById(R.id.spinnerAlertInterval);
        btnSave = findViewById(R.id.btnSave);

        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(
                this, R.array.alertRepeat, android.R.layout.simple_list_item_1);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlertRepeat.setAdapter(repeatAdapter);

        ArrayAdapter<CharSequence> intervalAdapter = ArrayAdapter.createFromResource(
                this, R.array.alertInterval, android.R.layout.simple_list_item_1);
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlertInterval.setAdapter(intervalAdapter);

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

    public void onSaveClicked(View v) {
        savePreferences();
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }

    private void savePreferences() {
        SharedPreferences preferences = getSharedPreferences("SOS_Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("voice_trigger", switchVoiceTrigger.isChecked());
        editor.putBoolean("sound_alert", switchSoundAlert.isChecked());
        editor.putBoolean("auto_location", switchAutoLocation.isChecked());
        editor.putString("custom_message", etCustomMessage.getText().toString());
        editor.putString("alert_repeat", spinnerAlertRepeat.getText().toString().trim());
        editor.putString("alert_interval", spinnerAlertInterval.getText().toString().trim());

        editor.apply(); // Save changes

        // Log sound alert status
        Log.d("SosSettings", "Sound Alert saved: " + switchSoundAlert.isChecked());
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
            spinnerAlertRepeat.setText(savedRepeat, false);
        }

        if (spinnerAlertInterval.getAdapter() != null) {
            int posInterval = ((android.widget.ArrayAdapter<String>) spinnerAlertInterval.getAdapter()).getPosition(savedInterval);
            spinnerAlertInterval.setText(savedInterval, false);
        }

        // Log loaded sound alert status
        Log.d("SosSettings", "Sound Alert loaded: " + preferences.getBoolean("sound_alert", false));
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
        boolean soundAlert = prefs.getBoolean("sound_alert", false);
        Toast.makeText(this, "Interval: " + interval + ", Repeat: " + repeat + ", Sound: " + soundAlert, Toast.LENGTH_LONG).show();
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
}