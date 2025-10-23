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
    private lateinit var shakeDetector: ShakeDetector
    private val emergencyNumber = "+14167046052"
        // "+16473305859"   // replace with your own
    private val REQUEST_PERMS = 100

    // if we asked for permission mid-flow, remember why we wanted to send
    private var pendingReason: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val fused = LocationServices.getFusedLocationProviderClient(this)
        sosManager = SosManager(this, fused, emergencyNumber)

        // initialize the shake detector
        shakeDetector = ShakeDetector(this) {
            // this lambda runs whenever a shake is detected
            sendAfterPermissions("shake")
        }

        val btnSOS = findViewById<Button>(R.id.btnSOS)
        val btnContacts = findViewById<Button>(R.id.btnContacts)
        val btnSafeLocations = findViewById<Button>(R.id.btnSafeLocations)
        val btnProfile = findViewById<Button>(R.id.btnProfile)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Back Button
        btnBack.setOnClickListener { finish() }

        // SOS Button
        btnSOS.setOnClickListener {
            sendAfterPermissions("button")
        }

        // (your other buttons unchanged)
        btnContacts.setOnClickListener { /* startActivity(Intent(...)) */ }
        btnSafeLocations.setOnClickListener { /* startActivity(Intent(...)) */ }
        btnProfile.setOnClickListener { /* startActivity(Intent(...)) */ }
        btnLogout.setOnClickListener { /* logout user */ }

        // request early so first tap/shake can work immediately
        requestDangerousPermissionsIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.start()   // start listening for shakes
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stop()    // stop listening to save battery
    }

    private fun sendAfterPermissions(reason: String) {
        // We require SEND_SMS to actually send the text.
        val hasSms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasSms) {
            pendingReason = reason
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_PERMS)
            return
        }

        // Location is optional (we’ll send a fallback if it’s not granted)
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasFine) {
            // request fine too, but we won't block sending on it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMS)
        }

        // Now actually send
        sosManager.triggerSOS(reason)
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
            val grantedMap = permissions.indices.associate { i ->
                permissions[i] to (grantResults[i] == PackageManager.PERMISSION_GRANTED)
            }

            // If SEND_SMS was just granted and we had a pending reason, finish the send now.
            if (grantedMap[Manifest.permission.SEND_SMS] == true) {
                pendingReason?.let {
                    sosManager.triggerSOS(it)
                    pendingReason = null
                }
            }

            val denied = permissions.indices.filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }
            if (denied.isNotEmpty()) {
                Toast.makeText(this, "Some permissions denied: $denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
