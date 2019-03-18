package com.unpi.madres.view

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.unpi.madres.R
import com.unpi.madres.map.VictimMapActivity
import kotlinx.android.synthetic.main.activity_choose_user.*

class ChooseUser : AppCompatActivity() {

    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_user)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        getLocationPermission()

        iamVictim.setOnClickListener {
            startActivity(Intent(this@ChooseUser, VictimMapActivity::class.java))
        }

        iamFamily.setOnClickListener {
            startActivity(Intent(this@ChooseUser, FamilyActivity::class.java))
        }

        iamVolunteer.setOnClickListener {
            startActivity(Intent(this@ChooseUser, VolunteerCircleActivity::class.java))
        }
    }

    private fun getLocationPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Membutuhkan Izin Lokasi", Toast.LENGTH_SHORT).show()
            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    1)
            }
        } else {
            // Permission has already been granted
            Toast.makeText(this, "Izin Lokasi diberikan", Toast.LENGTH_SHORT).show()
        }
    }
}
