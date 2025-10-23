package com.example.ma_final_project

import android.content.Context
import android.location.Location
import android.telephony.SmsManager
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient

class SosManager(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val emergencyNumber: String
) {
    /** Same behavior you had: try lastLocation; fallback to no-location message. */
    fun triggerSOS(reason: String = "button") {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    val message = if (location != null) {
                        "Emergency! I need help. My location: " +
                                "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                    } else {
                        "Emergency! I need help. Location unavailable."
                    }
                    sendSms(message)
                }
                .addOnFailureListener {
                    sendSms("Emergency! I need help. Location failed.")
                }
        } catch (e: SecurityException) {
            // Permissions not granted
            sendSms("Emergency! I need help. Location permission denied.")
        }
    }

    private fun sendSms(message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(emergencyNumber, null, message, null, null)
            Toast.makeText(context, "Emergency SMS sent!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "SMS failed: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}