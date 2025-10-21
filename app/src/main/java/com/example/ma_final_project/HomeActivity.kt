package com.example.ma_final_project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.content.Intent

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

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
            // TODO: Trigger SOS alert (location + SMS)
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
}