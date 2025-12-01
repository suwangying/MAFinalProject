package com.example.ma_final_project

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import android.widget.PopupMenu

class HomeActivity : AppCompatActivity() {
    companion object {
        private const val ID_FAKE_VIDEO = 1
        private const val ID_FAKE_VOICE = 2
    }
    private lateinit var sosManager: SosManager
    private lateinit var shakeDetector: ShakeDetector
    private val emergencyNumber = "+14167046052"
    private val REQUEST_PERMS = 100

    // if we asked for permission mid-flow, remember why we wanted to send
    private var pendingReason: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val fused = LocationServices.getFusedLocationProviderClient(this)
        val db = DatabaseHelper(this)
        sosManager = SosManager(
            context = this,
            fusedLocationClient = fused,
            fallbackNumber = emergencyNumber,
            dbHelper = db
        )

        // initialize the shake detector
        shakeDetector = ShakeDetector(this) {
            sendAfterPermissions("shake")
        }

        val btnSOS = findViewById<Button>(R.id.btnSOS)
        val btnContacts = findViewById<Button>(R.id.btnContacts)
        val btnSafeLocations = findViewById<Button>(R.id.btnSafeLocations)
        val btnProfile = findViewById<Button>(R.id.btnProfile)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnFakeCall = findViewById<Button>(R.id.btnFakeCall)
        val btnPhotoCapture = findViewById<Button>(R.id.btnPhotoCapture)

        btnPhotoCapture.setOnClickListener {
            startActivity(Intent(this, PhotoCaptureActivity::class.java))
        }


        // Back Button
        btnBack.setOnClickListener { finish() }

        // SOS Button
        btnSOS.setOnClickListener {
            sendAfterPermissions("button")
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // (your other buttons unchanged)
        btnContacts.setOnClickListener {
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
            /* startActivity(Intent(...)) */ }

        // Added Manage Safe Locations button action
        btnSafeLocations.setOnClickListener {
            val intent = Intent(this, ManageSafeLocationsActivity::class.java)
            startActivity(intent)
        }




        // request early so first tap/shake can work immediately
        requestDangerousPermissionsIfNeeded()

        btnFakeCall.setOnClickListener { anchor ->
            val popup = PopupMenu(this, anchor).apply {
                menu.add(0, ID_FAKE_VIDEO, 0, getString(R.string.fake_video_call))
                menu.add(0, ID_FAKE_VOICE, 1, getString(R.string.fake_voice_message))

                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        ID_FAKE_VIDEO -> {
                            startActivity(Intent(this@HomeActivity, IncomingCallActivity::class.java))
                            true
                        }
                        ID_FAKE_VOICE -> {
                            startActivity(Intent(this@HomeActivity, IncomingVoiceActivity::class.java))
                            true
                        }

                        else -> false
                    }
                }
            }
            popup.show()
        }
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
                Toast.makeText(this, "Some permissions denied: $denied", Toast.LENGTH_SHORT).show() }
        }
    }
}
