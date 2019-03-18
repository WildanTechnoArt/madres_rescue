package com.unpi.madres.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.unpi.madres.R;
import com.unpi.madres.database.SharedPrefManager;
import com.unpi.madres.models.VictimData;

public class VictimMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Deklarasi Variable
    private GoogleMap mMap;
    private String userID, userName, userNomor;
    private LocationRequest request;
    private Location mLocation;
    private FusedLocationProviderClient providerClient;
    private LocationCallback locationCallback;
    private Switch aSwitch;

    // Firebase
    private DatabaseReference toVolunteerRef;
    private DatabaseReference victimRef;
    private DatabaseReference victimDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.unpi.madres.R.layout.activity_maps);

        aSwitch = findViewById(R.id.shareLocation);

        // Firebase
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userName = SharedPrefManager.Companion.getInstance(this).getUser();
        userNomor = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        victimRef = FirebaseDatabase.getInstance().getReference("VictimList");
        victimDel = FirebaseDatabase.getInstance().getReference("VictimList")
                .child(userID);
        toVolunteerRef = FirebaseDatabase.getInstance().getReference("VolunteerCircle")
                .child(userID);

        initMap();

        startLocationUpdates();

        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                shareLocation();
            }else {
                toVolunteerRef.removeValue().addOnSuccessListener(aVoid ->
                        victimDel.removeValue().addOnSuccessListener(bVoid -> {
                            Toast.makeText(getApplicationContext(), "Share location di non-aktifkan", Toast.LENGTH_SHORT).show();
                            providerClient.removeLocationUpdates(locationCallback);
                        }));
            }
        });
    }

    private void initMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(VictimMapActivity.this);
    }

    private void buildLocationRequest() {
        request = new LocationRequest();
        request.setSmallestDisplacement(10f);
        request.setFastestInterval(3000);
        request.setInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void showDialog(){
        AlertDialog.Builder dialogAlert = new AlertDialog.Builder(this);
        dialogAlert.setTitle("Konfirmasi");
        dialogAlert.setMessage("Anda ingin keluar dari halaman ini?");
        dialogAlert.setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss());
        dialogAlert.setPositiveButton("Ya", (dialog, which) -> toVolunteerRef.removeValue()
                .addOnSuccessListener(aVoid -> victimDel.removeValue().addOnSuccessListener(bVoid -> {
            Toast.makeText(getApplicationContext(), "Share location di non-aktifkan", Toast.LENGTH_SHORT).show();
            finish();
        })));
        dialogAlert.setCancelable(false);
        dialogAlert.create();
        dialogAlert.show();
    }

    protected void startLocationUpdates() {
        buildLocationRequest();
        providerClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(getApplicationContext(), "Tidak dapat menemukan lokasi", Toast.LENGTH_SHORT).show();
                } else {
                    for (Location location : locationResult.getLocations()) {
                        mLocation = location;
                        onLocationChanged(location);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        providerClient.requestLocationUpdates(request, locationCallback, null);
    }

    private void shareLocation() {
        victimRef.child(userID).setValue(new VictimData(userName.toLowerCase(), userName, userNomor,
                String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()), "korban"))
                .addOnSuccessListener(aVoid ->
                        toVolunteerRef.setValue(new VictimData(userName.toLowerCase(), userName, userNomor,
                        String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()), "korban"))
                        .addOnSuccessListener(aVoid1 ->
                                Toast.makeText(getApplicationContext(), "Share location diaktifkan", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(getApplicationContext(), "Gagal membagikan lokasi", Toast.LENGTH_SHORT).show());
    }

    public void onLocationChanged(Location location) {
        mMap.clear();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(userName);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, (float) 16.0));
        mMap.addMarker(markerOptions);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            providerClient.removeLocationUpdates(locationCallback);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if(aSwitch.isChecked()){
            showDialog();
        }else {
            finish();
        }
    }
}