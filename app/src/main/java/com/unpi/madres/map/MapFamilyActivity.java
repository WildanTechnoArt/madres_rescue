package com.unpi.madres.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.*;
import com.unpi.madres.R;
import com.unpi.madres.database.SharedPrefManager;
import com.unpi.madres.models.VictimData;

import java.text.DecimalFormat;

public class MapFamilyActivity extends FragmentActivity implements OnMapReadyCallback {

    // Deklarasi
    private GoogleMap mMap;
    private DatabaseReference locations, victimRef;
    private String userNomor;
    private String userName;
    private Double lat, lng;
    private Boolean NoSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_volunteer);
        initMap();

       // String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        locations = FirebaseDatabase.getInstance().getReference().child("MyFamily");
        victimRef = FirebaseDatabase.getInstance().getReference("VictimList");
        userName = SharedPrefManager.Companion.getInstance(this).getUser();

        if (getIntent() != null) {
            userNomor = getIntent().getStringExtra("nomor");
            lat = getIntent().getDoubleExtra("lat", 0);
            lng = getIntent().getDoubleExtra("lng", 0);
            lng = getIntent().getDoubleExtra("lng", 0);
            NoSave = getIntent().getBooleanExtra("noSave", false);
        }
        if (NoSave) {
            loadLocationNoSaveUser(userNomor);
        }else {
            loadLocationForThisUser(userNomor);
        }
    }

    private void loadLocationNoSaveUser(String userNomor) {
        Query user_location = victimRef.orderByChild("nomorUser").equalTo(userNomor);

        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    VictimData tracking = postSnapshot.getValue(VictimData.class);

                    LatLng friendLocation = new LatLng(Double.parseDouble(tracking.getLat()),
                            Double.parseDouble(tracking.getLng()));

                    // Lokasi koordinat dari user
                    Location currentUser = new Location("");
                    currentUser.setLatitude(lat);
                    currentUser.setLongitude(lng);

                    // Lokasi koordiat dari teman
                    Location friend = new Location("");
                    friend.setLatitude(Double.parseDouble(tracking.getLat()));
                    friend.setLongitude(Double.parseDouble(tracking.getLng()));

                    mMap.clear();

                    // Menambahkan Keluarga Marker ke MAP
                    mMap.addMarker(new MarkerOptions()
                            .position(friendLocation)
                            .title(tracking.getNamaUser())
                            .snippet("Jarak " + new DecimalFormat("#.#").format((currentUser.distanceTo(friend)) / 1000) + "km")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16.0f));
                }

                MarkerOptions markerOptions = new MarkerOptions();
                LatLng current = new LatLng(lat, lng);
                markerOptions
                        .position(current)
                        .title(userName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                moveCamera(new LatLng(lat, lng));
                mMap.addMarker(markerOptions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void loadLocationForThisUser(String userNomor) {
        Query user_location = locations.orderByChild("nomorUser").equalTo(userNomor);

        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    VictimData tracking = postSnapshot.getValue(VictimData.class);

                    LatLng friendLocation = new LatLng(Double.parseDouble(tracking.getLat()),
                            Double.parseDouble(tracking.getLng()));

                    // Lokasi koordinat dari user
                    Location currentUser = new Location("");
                    currentUser.setLatitude(lat);
                    currentUser.setLongitude(lng);

                    // Lokasi koordiat dari teman
                    Location friend = new Location("");
                    friend.setLatitude(Double.parseDouble(tracking.getLat()));
                    friend.setLongitude(Double.parseDouble(tracking.getLng()));

                    mMap.clear();

                    // Menambahkan Keluarga Marker ke MAP
                    mMap.addMarker(new MarkerOptions()
                            .position(friendLocation)
                            .title(tracking.getNamaUser())
                            .snippet("Jarak " + new DecimalFormat("#.#").format((currentUser.distanceTo(friend)) / 1000) + "km")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16.0f));
                }

                MarkerOptions markerOptions = new MarkerOptions();
                LatLng current = new LatLng(lat, lng);
                markerOptions
                        .position(current)
                        .title(userName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                moveCamera(new LatLng(lat, lng));
                mMap.addMarker(markerOptions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void moveCamera(LatLng latLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, (float) 16.0));
    }

    private void initMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(MapFamilyActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }
}