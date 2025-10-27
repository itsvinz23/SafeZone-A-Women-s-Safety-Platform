package com.s23010921.safezone;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SafePlace extends AppCompatActivity implements OnMapReadyCallback {

    private Button btnSearch, btnNext;
    private TextView txtSearch;
    private int currentIndex = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private LatLng currentLocation;

    // Array of place types to search
    private final String[] places = {
            "police station near me",
            "hospital near me",
            "temple near me",
            "church near me",
            "pharmacy near me"
    };

    // Location permission launcher
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_place);

        // Initialize UI components
        btnSearch = findViewById(R.id.btnSearch);
        btnNext = findViewById(R.id.btnNext);
        txtSearch = findViewById(R.id.etSearch);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Map fragment setup
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set initial place text
        txtSearch.setText(places[currentIndex]);

        // Request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            fetchLocation();
        }

        // Handle Next button
        btnNext.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % places.length;
            txtSearch.setText(places[currentIndex]);
        });

        // Handle Search button
        btnSearch.setOnClickListener(v -> {
            if (currentLocation != null) {
                // Search with current location
                String query = places[currentIndex];
                String locationParam = "&query=" + currentLocation.latitude + "," + currentLocation.longitude;
                try {
                    Uri uri = Uri.parse("https://www.google.com/maps/search/" +
                            Uri.encode(query) + "/@" +
                            currentLocation.latitude + "," +
                            currentLocation.longitude + ",15z");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Fallback: open in browser
                    Uri uri = Uri.parse("https://www.google.com/maps/search/" +
                            Uri.encode(query) + "/@" +
                            currentLocation.latitude + "," +
                            currentLocation.longitude + ",15z");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            } else {
                // Fallback to original search without location
                String query = places[currentIndex];
                try {
                    Uri uri = Uri.parse("https://www.google.com/maps/search/" + Uri.encode(query));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "Google Maps app not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView homeIcon = findViewById(R.id.homeIcon);
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SafePlace.this, DashBoard.class);
            startActivity(intent);
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                updateCurrentLocation(location);
                showLocationOnMap(location);
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentLocation(Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                // You can display this address in a TextView if needed
                Toast.makeText(this, "Location found: " + address, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLocationOnMap(Location location) {
        if (mMap == null) return;

        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
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