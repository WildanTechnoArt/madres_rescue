package com.unpi.madres.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.unpi.madres.R
import com.unpi.madres.adapter.FamilyCircleHolder
import com.unpi.madres.map.MapFamilyActivity
import com.unpi.madres.models.VictimData
import kotlinx.android.synthetic.main.activity_family_circle.*

class FamilyCircleActivity : AppCompatActivity() {

    private var mAuth: FirebaseUser? = null
    private var familyRefrence: DatabaseReference? = null
    private var recyclerAdapter: FirebaseRecyclerAdapter<VictimData, FamilyCircleHolder>? = null
    private var listVolunteer: RecyclerView? = null
    private val userName: String? = null
    private val userNomor: String? = null
    private var mLocation: Location? = null
    private var request: LocationRequest? = null
    private var providerClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private val progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_circle)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //Inisialisasi dan Instance FirebaseUser(getUser)
        mAuth = FirebaseAuth.getInstance().currentUser

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Inisilasisasi
        familyList.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this@FamilyCircleActivity)
        familyList.layoutManager = layoutManager

        familyRefrence = FirebaseDatabase.getInstance().reference.child("MyFamily").child(mAuth?.uid.toString())

        displayLocation()

        updateList()
    }

    private fun buildLocationRequest() {
        request = LocationRequest()
        request?.smallestDisplacement = 10f
        request?.fastestInterval = 3000
        request?.interval = 5000
        request?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun displayLocation() {
        buildLocationRequest()
        providerClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    Toast.makeText(applicationContext, "Tidak dapat menemukan lokasi", Toast.LENGTH_SHORT).show()
                } else {
                    for (location in locationResult.locations) {
                        mLocation = location
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        providerClient?.requestLocationUpdates(request, locationCallback, null)
    }

    private fun updateList() {

        val options = familyRefrence?.let {
            FirebaseRecyclerOptions.Builder<VictimData>()
                .setQuery(it, VictimData::class.java)
                .setLifecycleOwner(this)
                .build()
        }

        recyclerAdapter = object : FirebaseRecyclerAdapter<VictimData, FamilyCircleHolder>(options!!) {

            override fun onCreateViewHolder(parent: ViewGroup, i: Int): FamilyCircleHolder {
                return FamilyCircleHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.family_item_layout, parent, false)
                )
            }

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: FamilyCircleHolder, position: Int, model: VictimData) {

                holder.NameVictim.text = model.namaUser!! + " (Korban)"
                holder.NomorVictim.text = model.nomorUser

                holder.setiRecyclerItemClickListener { _, _ ->

                    val action = arrayOf("Lihat Lokasi", "Hapus Data")
                    val alert = AlertDialog.Builder(this@FamilyCircleActivity)
                    alert.setTitle("Pilih Opsi")
                    alert.setItems(action) { _, i ->
                        when (i) {
                            0 -> {
                                val map = Intent(this@FamilyCircleActivity, MapFamilyActivity::class.java)
                                map.putExtra("nomor", model.nomorUser)
                                map.putExtra("lat", mLocation?.latitude)
                                map.putExtra("lng", mLocation?.longitude)
                                map.putExtra("noSave", true)
                                startActivity(map)
                            }
                            1 -> {
                                val getKey: String = getRef(holder.adapterPosition).key.toString()

                                familyRefrence?.child(getKey)?.removeValue()
                                    ?.addOnSuccessListener {
                                        Toast.makeText(this@FamilyCircleActivity, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    alert.create()
                    alert.show()
                }
            }
        }
        recyclerAdapter?.startListening()
        familyList?.adapter = recyclerAdapter
        recyclerAdapter?.notifyDataSetChanged()
    }

    override fun onStop() {
        recyclerAdapter?.stopListening()
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        providerClient?.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        recyclerAdapter?.startListening()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}