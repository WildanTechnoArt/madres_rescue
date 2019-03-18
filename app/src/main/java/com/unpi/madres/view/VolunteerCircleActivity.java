package com.unpi.madres.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.unpi.madres.R;
import com.unpi.madres.adapter.VolunteerCircleHolder;
import com.unpi.madres.database.SharedPrefManager;
import com.unpi.madres.map.MapVolunteerActivity;
import com.unpi.madres.models.VictimData;
import com.unpi.madres.models.VolunteerData;

public class VolunteerCircleActivity extends AppCompatActivity {

    // Firebase
    private DatabaseReference toVolunteerRef, userReferences, volunterList;
    private FirebaseRecyclerAdapter<VictimData, VolunteerCircleHolder> recyclerAdapter;
    private RecyclerView listVolunteer;
    private String userID, userName, userNomor;
    private Location mLocation;
    private LocationRequest request;
    private FusedLocationProviderClient providerClient;
    private LocationCallback locationCallback;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_circle);

        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inisilasisasi
        listVolunteer = findViewById(R.id.victimList);
        listVolunteer.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listVolunteer.setLayoutManager(layoutManager);

        // Firebase
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userName = SharedPrefManager.Companion.getInstance(this).getUser();
        userNomor = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        toVolunteerRef = FirebaseDatabase.getInstance().getReference("VolunteerCircle");
        volunterList = FirebaseDatabase.getInstance().getReference("VolunteerList");
        userReferences = FirebaseDatabase.getInstance().getReference("VolunteerCircle")
                .child(userID);

        showDialog();

        displayLocation();

        updateList();
    }

    private void showDialog(){
        AlertDialog.Builder dialogAlert = new AlertDialog.Builder(this);
        dialogAlert.setTitle("Konfirmasi");
        dialogAlert.setMessage("Tambahkan sebagai Relawan?");
        dialogAlert.setNegativeButton("Tidak", (dialog, which) -> finish());
        dialogAlert.setPositiveButton("Ya", (dialog, which) -> setupSystem());
        dialogAlert.setCancelable(false);
        dialogAlert.create();
        dialogAlert.show();
    }

    private void buildLocationRequest() {
        request = new LocationRequest();
        request.setSmallestDisplacement(10f);
        request.setFastestInterval(3000);
        request.setInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void displayLocation() {
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

    private void updateList(){

        FirebaseRecyclerOptions<VictimData> options = new FirebaseRecyclerOptions.Builder<VictimData>()
                .setQuery(toVolunteerRef, VictimData.class)
                .setLifecycleOwner(this)
                .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<VictimData, VolunteerCircleHolder>(options){

            @NonNull
            @Override
            public VolunteerCircleHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                return new VolunteerCircleHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.victim_item_layout, parent, false));
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull VolunteerCircleHolder holder, int position, @NonNull VictimData model) {

                // Data Korban
                if (model.getVictimStatus().equals("korban")){
                    holder.NameVictim.setText(model.getNamaUser() +" (Korban)");
                    holder.NomorVictim.setText(model.getNomorUser());
                    holder.StatusVictim.setImageResource(R.drawable.ic_offline);

                // Data Relawan
                }else {
                    holder.NameVictim.setText(model.getNamaUser()+" (Relawan)");
                    holder.NomorVictim.setText(model.getNomorUser());
                    holder.StatusVictim.setImageResource(R.drawable.ic_online);
                }

                holder.setiRecyclerItemClickListener((view, position1) -> {
                    if(!model.getNomorUser().equals(userNomor)){
                        Intent map = new Intent(VolunteerCircleActivity.this, MapVolunteerActivity.class);
                        map.putExtra("nomor", model.getNomorUser());
                        map.putExtra("lat", mLocation.getLatitude());
                        map.putExtra("lng", mLocation.getLongitude());
                        map.putExtra("status", model.getVictimStatus());
                        startActivity(map);
                    }
                });
            }
        };
        recyclerAdapter.startListening();
        listVolunteer.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();
    }

    private void setupSystem(){
        progressBar.setVisibility(View.VISIBLE);
        toVolunteerRef.child(userID).setValue(new VolunteerData(userName.toLowerCase(), userName, userNomor,
                String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()), "relawan"))
                .addOnSuccessListener(aVoid ->
                        volunterList.child(userID).setValue(new VolunteerData(userName.toLowerCase(), userName, userNomor,
                                String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()), "relawan"))
                                .addOnSuccessListener(aVoid1 ->{
                                    Toast.makeText(getApplicationContext(), "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }))
                .addOnFailureListener(e ->
                        Toast.makeText(getApplicationContext(), "Gagal membagikan lokasi", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStop() {
        if(recyclerAdapter != null){
            recyclerAdapter.stopListening();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        providerClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(recyclerAdapter != null){
            recyclerAdapter.startListening();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userReferences.removeValue();
        volunterList.child(userID).removeValue();
    }
}