package com.example.ma_final_project

import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class AddSafeLocationActivity : AppCompatActivity() {
    // UI elements for entering the location name + address
    private lateinit var etLocationName: EditText
    private lateinit var etLocationAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button
    // Database helper for saving the safe location into SQLite
    private lateinit var db: DatabaseHelper

    // Flags used when editing an existing location instead of adding a new one
    private var updateMode = false
    private var locationId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_safe_location)

        // Connecting UI fields to their views
        etLocationName = findViewById(R.id.etLocationName)
        etLocationAddress = findViewById(R.id.etAddress)
        btnSave = findViewById(R.id.btnSaveLocation)
        btnBack = findViewById(R.id.btnBack)
        db = DatabaseHelper(this)

        // Check if this is an update
        updateMode = intent.getBooleanExtra("updateMode", false)
        if (updateMode) {
            // Load the previous data into the text fields
            locationId = intent.getIntExtra("locationId", -1)
            etLocationName.setText(intent.getStringExtra("name"))
            etLocationAddress.setText(intent.getStringExtra("address"))
            btnSave.text = "Update Location"
        }

        // When user presses Save or Update button
        btnSave.setOnClickListener {
            val name = etLocationName.text.toString().trim()
            val address = etLocationAddress.text.toString().trim()
            // Basic input validation
            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please enter both name and address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                // Geocoder converts the written address into latitude & longitude
                val geocoder = Geocoder(this, Locale.getDefault())
                val results = geocoder.getFromLocationName(address, 1)
                // If geocoder found a matching location
                if (!results.isNullOrEmpty()) {
                    val lat = results[0].latitude
                    val lng = results[0].longitude

                    // Update existing location in DB
                    if (updateMode && locationId != -1) {
                        db.updateLocation(locationId, name, address, lat, lng)
                        Toast.makeText(this, "Location updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        db.addLocation("9999999999", name, address, lat, lng)
                        Toast.makeText(this, "Location added successfully", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                } else {
                    Toast.makeText(this, "Could not find coordinates for that address", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error finding location", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}
