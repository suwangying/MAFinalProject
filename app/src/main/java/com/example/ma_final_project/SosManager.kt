package com.example.ma_final_project

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource

class SosManager(
    private val context: Context,
    private val fused: FusedLocationProviderClient,
    private val emergencyNumber: String
) {
    fun triggerSOS(reason: String = "button") {
        fetchBestLocation { loc ->
            val msg = buildMessage(reason, loc)
            sendSms(msg)
        }
    }

    private fun fetchBestLocation(onResult: (Location?) -> Unit) {
        // If no location permission, just send fallback message.
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            onResult(null); return
        }

        // 1) Try a fresh single fix (best effort, quick).
        val cts = CancellationTokenSource()
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    onResult(loc)
                } else {
                    // 2) Fall back to last known.
                    fused.lastLocation
                        .addOnSuccessListener { last -> onResult(last) }
                        .addOnFailureListener { onResult(null) }
                }
            }
            .addOnFailureListener {
                // 3) If current location failed, try last known anyway.
                fused.lastLocation
                    .addOnSuccessListener { last -> onResult(last) }
                    .addOnFailureListener { onResult(null) }
            }

        // Optional: prompt user to enable location if it's off (no hard fail if they cancel)
        ensureLocationSettings()
    }

    private fun ensureLocationSettings() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3_000L).build()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(request)
            .setAlwaysShow(true)
            .build()

        val client = LocationServices.getSettingsClient(context)
        client.checkLocationSettings(settingsRequest)
            .addOnFailureListener { ex ->
                if (ex is ResolvableApiException && context is Activity) {
                    try {
                        ex.startResolutionForResult(context, 0xBEEF) // any request code
                    } catch (_: IntentSender.SendIntentException) { /* ignore */ }
                }
            }
    }

    private fun buildMessage(reason: String, loc: Location?): String {
        return if (loc != null) {
            "🚨 Emergency ($reason)! I need help.\n" +
                    "Location: https://www.google.com/maps/search/?api=1&query=${loc.latitude},${loc.longitude}"
        } else {
            "🚨 Emergency ($reason)! I need help. Location unavailable."
        }
    }

    private fun sendSms(message: String) {
        try {
            val sms = SmsManager.getDefault()
            sms.sendTextMessage(emergencyNumber, null, message, null, null)
            Toast.makeText(context, "Emergency SMS sent!", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, "Missing SEND_SMS permission", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "SMS failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
