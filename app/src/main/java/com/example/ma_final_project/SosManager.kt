package com.example.ma_final_project

import android.Manifest
import android.content.Context
import android.location.Location
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import android.util.Log

class SosManager(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val emergencyNumber: String
) {

    fun triggerSOS(reason: String = "button") {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            sendSms("🚨 Emergency ($reason)! I need help. Location permission denied.")
            return
        }

        try {
            // Try last known location first
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        sendSmsWithLocation(loc, reason)
                    } else {
                        // Request a fresh location if lastLocation is null
                        val locationRequest = LocationRequest.Builder(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            1000L
                        ).setMaxUpdates(1).build()

                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            object : LocationCallback() {
                                override fun onLocationResult(result: LocationResult) {
                                    fusedLocationClient.removeLocationUpdates(this)
                                    val newLoc = result.lastLocation
                                    if (newLoc != null)
                                        sendSmsWithLocation(newLoc, reason)
                                    else
                                        sendSms("🚨 Emergency ($reason)! I need help. Location unavailable.")
                                }
                            },
                            null
                        )
                    }
                }
                .addOnFailureListener {
                    sendSms("🚨 Emergency ($reason)! I need help. Location retrieval failed.")
                }
        } catch (e: SecurityException) {
            sendSms("🚨 Emergency ($reason)! I need help. Location permission denied.")
        }
    }

    private fun sendSmsWithLocation(loc: Location, reason: String) {
        val msg = "🚨 Emergency ($reason)! I need help.\n" +
                "Location: https://www.google.com/maps/search/?api=1&query=${loc.latitude},${loc.longitude}"
        sendSms(msg)
        Log.d("SOS", "Latitude: ${loc.latitude}, Longitude: ${loc.longitude}")
        Toast.makeText(context, "Lat: ${loc.latitude}, Lng: ${loc.longitude}", Toast.LENGTH_LONG).show()
    }

    private fun sendSms(message: String) {
        try {
            val sms = SmsManager.getDefault()
            sms.sendTextMessage(emergencyNumber, null, message, null, null)
            Toast.makeText(context, "Emergency SMS sent!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "SMS failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
