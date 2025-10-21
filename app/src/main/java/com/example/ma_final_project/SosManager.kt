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
    fun triggerSOS(reason: String = "button") {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc: Location? ->
                    val msg = if (loc != null) {
                        "🚨 Emergency ($reason)! I need help.\n" +
                                "Location: https://www.google.com/maps/search/?api=1&query=${loc.latitude},${loc.longitude}"
                    } else {
                        "🚨 Emergency ($reason)! I need help. Location unavailable."
                    }
                    sendSms(msg)
                }
                .addOnFailureListener {
                    sendSms("🚨 Emergency ($reason)! I need help. Location retrieval failed.")
                }
        } catch (e: SecurityException) {
            sendSms("🚨 Emergency ($reason)! I need help. Location permission denied.")
        }
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
