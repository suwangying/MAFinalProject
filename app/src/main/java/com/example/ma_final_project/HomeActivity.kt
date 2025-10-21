package com.example.ma_final_project

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

class HomeActivity : AppCompatActivity() {
    private lateinit var sosManager: SosManager
    private val emergencyNumber = "+14167046052"  // replace with your own
    private val REQUEST_PERMS = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val fused = LocationServices.getFusedLocationProviderClient(this)
        sosManager = SosManager(this, fused, emergencyNumber)

        val btnSOS = findViewById<Button>(R.id.btnSOS)
        val btnContacts = findViewById<Button>(R.id.btnContacts)
        val btnSafeLocations = findViewById<Button>(R.id.btnSafeLocations)
        val btnProfile = findViewById<Button>(R.id.btnProfile)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnLogout = findViewById<Button>(R.id.btnLogout)


        // Back Button
        btnBack.setOnClickListener {
            finish()
        }

        // SOS Button
        btnSOS.setOnClickListener {
            requestDangerousPermissionsIfNeeded()
            sosManager.triggerSOS("button")
        }

        // Emergency Contacts
        btnContacts.setOnClickListener {
            /*val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)*/
        }

        // Safe Locations
        btnSafeLocations.setOnClickListener {
            /*val intent = Intent(this, SafeLocationsActivity::class.java)
            startActivity(intent)*/
        }

        // User Profile
        btnProfile.setOnClickListener {
            /*val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)*/
        }

        btnLogout.setOnClickListener {
            // TODO: Logout User
        }

    }
    private fun requestDangerousPermissionsIfNeeded() {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) needed += Manifest.permission.SEND_SMS

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) needed += Manifest.permission.ACCESS_FINE_LOCATION

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), REQUEST_PERMS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMS) {
            val denied = grantResults.indices.filter {
                grantResults[it] != PackageManager.PERMISSION_GRANTED
            }
            if (denied.isNotEmpty()) {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}