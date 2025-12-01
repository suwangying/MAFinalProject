package com.example.ma_final_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

// This screen shows all the user's saved safe locations
// and lets them add, update, delete, or open them in Google Maps.

class ManageSafeLocationsActivity : AppCompatActivity() {

    private val userPhone = "9999999999"
    private lateinit var db: DatabaseHelper
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_safe_locations)

        val backButton = findViewById<ImageView>(R.id.backButton)
        val addButton = findViewById<Button>(R.id.btnAddLocation)
        container = findViewById(R.id.safeLocationsContainer)
        db = DatabaseHelper(this)

        backButton.setOnClickListener { finish() }
        addButton.setOnClickListener {
            startActivity(Intent(this, AddSafeLocationActivity::class.java))
        }

        loadLocations()
    }
    // Reads safe locations from DB and dynamically builds UI cards for each one
    private fun loadLocations() {
        container.removeAllViews()
        val cursor = db.getLocationsForUser(userPhone)

        if (cursor.count == 0) {
            val emptyText = TextView(this).apply {
                text = "No Safe Locations Added Yet"
                setTextColor(ContextCompat.getColor(this@ManageSafeLocationsActivity, android.R.color.white))
                textSize = 16f
            }
            container.addView(emptyText)
        } else {
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOCATION_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOCATION_NAME))
                val address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOCATION_ADDRESS))
                val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOCATION_LAT))
                val lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOCATION_LNG))

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor(ContextCompat.getColor(this@ManageSafeLocationsActivity, R.color.colorButton))
                    setPadding(20, 20, 20, 20)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 30)
                    layoutParams = params
                }

                val tv = TextView(this).apply {
                    text = "🏠 $name\n📍 $address"
                    setTextColor(ContextCompat.getColor(this@ManageSafeLocationsActivity, android.R.color.white))
                    textSize = 16f
                }

                val buttonRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                val updateBtn = Button(this).apply {
                    text = "Update"
                    setBackgroundColor(ContextCompat.getColor(this@ManageSafeLocationsActivity, R.color.purple_500))
                    setTextColor(ContextCompat.getColor(this@ManageSafeLocationsActivity, android.R.color.white))
                    setOnClickListener {
                        val intent = Intent(this@ManageSafeLocationsActivity, AddSafeLocationActivity::class.java)
                        intent.putExtra("updateMode", true)
                        intent.putExtra("locationId", id)
                        intent.putExtra("name", name)
                        intent.putExtra("address", address)
                        startActivity(intent)
                    }
                }

                val deleteBtn = Button(this).apply {
                    text = "Delete"
                    setBackgroundColor(ContextCompat.getColor(this@ManageSafeLocationsActivity, R.color.colorSOS))
                    setTextColor(ContextCompat.getColor(this@ManageSafeLocationsActivity, android.R.color.white))
                    setOnClickListener {
                        val rows = db.deleteLocation(id)
                        if (rows > 0) {
                            Toast.makeText(this@ManageSafeLocationsActivity, "Deleted successfully", Toast.LENGTH_SHORT).show()
                            loadLocations()
                        }
                    }
                }

                val mapBtn = Button(this).apply {
                    text = "Map"
                    setBackgroundColor(ContextCompat.getColor(this@ManageSafeLocationsActivity, R.color.light_blue_A400))
                    setTextColor(ContextCompat.getColor(this@ManageSafeLocationsActivity, android.R.color.white))
                    setOnClickListener {
                        openGoogleMaps(lat, lng)
                    }
                }

                buttonRow.addView(updateBtn)
                buttonRow.addView(deleteBtn)
                buttonRow.addView(mapBtn)

                card.addView(tv)
                card.addView(buttonRow)
                container.addView(card)
            }
        }
        cursor.close()
    }
// Opens Google Maps app with driving directions to the given coordinates
    private fun openGoogleMaps(lat: Double, lng: Double) {
        try {
            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=driving")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadLocations()
    }
}
