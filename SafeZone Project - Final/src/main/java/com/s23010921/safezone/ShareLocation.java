package com.s23010921.safezone;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ShareLocation extends AppCompatActivity implements OnMapReadyCallback {

    private TextView tvLatitude, tvLongitude, tvAddress;
    private Button btnSendMessage, btnSendOther;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;

    // Location permission launcher
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    // SMS permission launcher
    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new RequestPermission(), isGranted -> {
                if (isGranted) {
                    sendLocationToPriorityContacts(); // Retry sending after permission granted
                } else {
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_location);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAddress = findViewById(R.id.tvAddress);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnSendOther = findViewById(R.id.btnSendOther);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Map fragment setup
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            fetchLocation();
        }

        // Send via Message -> request SMS permission if not granted
        btnSendMessage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
            } else {
                sendLocationToPriorityContacts();
            }
        });

        // Send via Other -> share with apps
        btnSendOther.setOnClickListener(v -> {
            String latitude = tvLatitude.getText().toString();
            String longitude = tvLongitude.getText().toString();
            String locationUrl = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
            String message = "📍 My current location:\nLatitude: " + latitude +
                    "\nLongitude: " + longitude +
                    "\nAddress: " + tvAddress.getText().toString() +
                    "\nMap: " + locationUrl;

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(sendIntent, "Share location via"));
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                updateLocationUI(location);
                showLocationOnMap(location);
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLocationUI(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        tvLatitude.setText(String.valueOf(latitude));
        tvLongitude.setText(String.valueOf(longitude));

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                tvAddress.setText(addresses.get(0).getAddressLine(0));
            } else {
                tvAddress.setText("Address not found");
            }
        } catch (IOException e) {
            tvAddress.setText("Error fetching address");
            e.printStackTrace();
        }
    }

    private void showLocationOnMap(Location location) {
        if (mMap == null) return;

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
    }

    private void sendLocationToPriorityContacts() {
        DBHelper dbHelper = new DBHelper(this);
        List<ContactModel> allContacts = dbHelper.getAllContacts();

        if (allContacts.isEmpty()) {
            Toast.makeText(this, "No contacts available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if device can send SMS
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            Toast.makeText(this, "Device cannot send SMS", Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.sort(allContacts, Comparator.comparingInt(c -> Integer.parseInt(c.getPriority())));
        int count = Math.min(5, allContacts.size());

        String latitude = tvLatitude.getText().toString();
        String longitude = tvLongitude.getText().toString();
        String address = tvAddress.getText().toString();
        String locationUrl = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;

        String message = "📍 My current location - \n"+
                "\nAddress: " + address +
                "\nMap: " + locationUrl;

        int successfulSends = 0;
        SmsManager smsManager = SmsManager.getDefault();

        for (int i = 0; i < count; i++) {
            ContactModel contact = allContacts.get(i);
            String number = contact.getNumber().trim();

            // Validate phone number
            if (number.isEmpty() || !android.util.Patterns.PHONE.matcher(number).matches()) {
                continue;
            }

            try {
                // Create pending intents for delivery reports
                PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0,
                        new Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE);

                PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0,
                        new Intent("SMS_DELIVERED"), PendingIntent.FLAG_IMMUTABLE);

                // Split long messages
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(number, null, parts, null, null);

                successfulSends++;
                Log.d("SMS", "Sent to: " + number);
            } catch (Exception e) {
                Log.e("SMS", "Failed to send to " + number, e);
            }
        }

        if (successfulSends > 0) {
            Toast.makeText(this, "Location sent to " + successfulSends + " contacts", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to send to any contacts", Toast.LENGTH_LONG).show();
        }
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

