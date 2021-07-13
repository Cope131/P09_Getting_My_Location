package com.myapplicationdev.android.p09gettingmylocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private final String DEBUG_TAG = MainActivity.class.getSimpleName();

    private final int REQUEST_CODE = 101;

    // Views
    private Button checkRecordsBtn, getLocationUpdateBtn, removeLocationUpdateBtn;
    private TextView lngTV, latTV;
    private GoogleMap googleMap;

    // Location
    private Location lastLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    // Map
    private Marker lastLocationMarker;

    // File Directory to Save Records
    private String folderLocPath;
    private File recordFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        folderLocPath = getFilesDir().getAbsolutePath() + "/LocationRecords";
        initViews();
        initMap();
        askPermission();
    }

    private void initViews() {
        checkRecordsBtn = findViewById(R.id.check_records_button);
        getLocationUpdateBtn = findViewById(R.id.get_location_update_button);
        removeLocationUpdateBtn = findViewById(R.id.remove_location_update_button);
        lngTV = findViewById(R.id.longitude_text_view);
        latTV = findViewById(R.id.latitude_text_view);
        checkRecordsBtn.setOnClickListener(this);
        getLocationUpdateBtn.setOnClickListener(this);
        removeLocationUpdateBtn.setOnClickListener(this);
    }

    // --- Click Button ---
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_records_button:
                checkRecords();
                break;
            case R.id.get_location_update_button:
                getLocationUpdate();
                break;
            case R.id.remove_location_update_button:
                removeLocationUpdates();
        }
    }

    private void checkRecords() {
        Intent intent = new Intent(this, RecordsActivity.class);
        intent.putExtra("folderPath", folderLocPath);
        intent.putExtra("fileName", "records.txt");
        startActivity(intent);
    }

    private void removeLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Toast.makeText(this, "Location Update Disabled", Toast.LENGTH_SHORT).show();
    }

    private void getLocationUpdate() {
        initLocationComp();
        Toast.makeText(this, "Location Update Enabled", Toast.LENGTH_SHORT).show();
    }

    private void changeLocationRequestProps() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        locationRequest.setInterval(30 * 1000).setSmallestDisplacement(500 * 10);
    }

    // --- Location Request ---
    private void initLocationComp() {
        // Connect to google play location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Location Request Settings
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000)
                .setSmallestDisplacement(100);
        // Listener for Location Updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    lastLocation = locationResult.getLastLocation();
                    double lat = lastLocation.getLatitude();
                    double lng = lastLocation.getLongitude();
                    latTV.setText(String.valueOf(lat));
                    lngTV.setText(String.valueOf(lng));
                    Log.d(DEBUG_TAG, "Lat: " + lat + " Lang: " + lng);
                    showMarkerLastLocation();
                    saveToDir();
                } else {
                    Toast.makeText(MainActivity.this, "Location Update is Disabled", Toast.LENGTH_SHORT).show();
                }
            }
        };
        // Start Requesting for Location Updates
        if (checkPermission()) {
            Log.d(DEBUG_TAG, "Check Permission Result: " + checkPermission());
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    // --- Permissions ---
    private void askPermission() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE);
            return;
        }
        initLocationComp();
    }

    private boolean checkPermission() {
        int permissionCheck_Coarse
                = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine
                = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED ||
                permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(DEBUG_TAG, "requestCode: " + requestCode);
        Log.d(DEBUG_TAG, "permissions: " + Arrays.toString(permissions));
        Log.d(DEBUG_TAG, "grantResults: " + Arrays.toString(grantResults));
         if (requestCode == REQUEST_CODE) {
             // All Permissions Granted
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initLocationComp();
            }  else {
                Snackbar
                        .make(findViewById(android.R.id.content), "Location Permission was not granted",
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction("View", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                askPermission();
                            }
                        })
                        .show();
            }
        }
    }

    // --- Map ---
    private void initMap() {
        SupportMapFragment mapFragment
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (googleMap != null) {
            // Enable Features
            UiSettings uiSettings = googleMap.getUiSettings();
            uiSettings.setCompassEnabled(true);
            uiSettings.setZoomControlsEnabled(true);
            uiSettings.setZoomGesturesEnabled(true);
        }
    }

    private void showMarkerLastLocation() {
        if (lastLocationMarker != null) {
            lastLocationMarker.remove();
        }
        if (googleMap != null) {
            // Add Marker
            LatLng position = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            lastLocationMarker = googleMap.addMarker(markerOptions);
            // Zoom into Marker
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 11));
        }
    }

    // --- File Record ---
    private void saveToDir() {
        createDir();
        writeRecord();
    }

    private void createDir() {
        Log.d(DEBUG_TAG, folderLocPath);
        File folder = new File(folderLocPath);
        if (!folder.exists()) {
            boolean isCreated = folder.mkdir();
            Log.d(DEBUG_TAG, isCreated ? "Folder Created" : "Folder Not Created");
        }
    }

    private void writeRecord() {
        recordFile = new File(folderLocPath, "records.txt");
        Log.d(DEBUG_TAG, folderLocPath + recordFile.getAbsolutePath());
        try {
            FileWriter writer = new FileWriter(recordFile, true);
            String location =lastLocation.getLatitude() + ", " + lastLocation.getLongitude();
            writer.write(location + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to write!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }




}