package com.s23010921.safezone;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class SoundUtility implements TextToSpeech.OnInitListener {
    private Context context;
    private TextToSpeech textToSpeech;
    private MediaPlayer mediaPlayer;
    private boolean isTTSInitialized = false;

    public SoundUtility(Context context) {
        this.context = context;
        initializeTextToSpeech();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported");
                Toast.makeText(context, "TTS Language not supported", Toast.LENGTH_SHORT).show();
            } else {
                isTTSInitialized = true;
                Log.d("TTS", "TextToSpeech initialized successfully");
            }
        } else {
            Log.e("TTS", "TextToSpeech initialization failed");
        }
    }

    public void playEmergencySound() {
        try {
            // Stop any existing sound first
            stopEmergencySound();

            // Create and start emergency sound
            // First try to use a raw resource
            int soundResource = context.getResources().getIdentifier(
                    "emergency_alert_sound", "raw", context.getPackageName());

            if (soundResource != 0) {
                mediaPlayer = MediaPlayer.create(context, soundResource);
            } else {
                // If no custom sound, use system alarm sound
                mediaPlayer = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
            }

            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true); // Loop the sound
                mediaPlayer.setVolume(1.0f, 1.0f); // Maximum volume
                mediaPlayer.start();
                Log.d("SoundUtility", "Emergency sound started");
            } else {
                Log.e("SoundUtility", "Failed to create media player");
            }
        } catch (Exception e) {
            Log.e("SoundUtility", "Error playing emergency sound: " + e.getMessage());
            Toast.makeText(context, "Error playing emergency sound", Toast.LENGTH_SHORT).show();
        }
    }

    public void speakEmergencyMessage(String message) {
        if (isTTSInitialized && textToSpeech != null) {
            try {
                // Stop any previous speech
                textToSpeech.stop();

                // Speak the emergency message
                textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, "emergency_tts");
                Log.d("SoundUtility", "TTS speaking: " + message);
            } catch (Exception e) {
                Log.e("SoundUtility", "Error with TTS: " + e.getMessage());
            }
        } else {
            Log.e("SoundUtility", "TTS not initialized");
        }
    }

    public void stopEmergencySound() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                Log.d("SoundUtility", "Emergency sound stopped");
            }
        } catch (Exception e) {
            Log.e("SoundUtility", "Error stopping sound: " + e.getMessage());
        }
    }

    public void stopTTS() {
        try {
            if (textToSpeech != null) {
                textToSpeech.stop();
                // Don't shutdown here to allow reuse, only shutdown in cleanup
            }
        } catch (Exception e) {
            Log.e("SoundUtility", "Error stopping TTS: " + e.getMessage());
        }
    }

    public void cleanup() {
        stopEmergencySound();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        Log.d("SoundUtility", "Sound utility cleaned up");
    }

    public boolean isSoundAlertEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("SOS_Settings", Context.MODE_PRIVATE);
        return prefs.getBoolean("sound_alert", false);
    }

    public boolean isTTSReady() {
        return isTTSInitialized;
    }
}