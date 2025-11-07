package com.example.ma_final_project

import android.content.Context
import android.location.Location
import android.telephony.SmsManager
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient

class SosManager(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val fallbackNumber: String,               // your old single number
    private val dbHelper: DatabaseHelper              // NEW: to fetch contacts
) {

    fun triggerSOS(reason: String = "button") {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    val locPart = if (location != null) {
                        "My location: https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                    } else {
                        "Location unavailable."
                    }
                    val message = "Emergency! I need help (via $reason). $locPart"
                    sendToAllContactsOrFallback(message)
                }
                .addOnFailureListener {
                    val message = "Emergency! I need help (via $reason). Location failed."
                    sendToAllContactsOrFallback(message)
                }
        } catch (_: SecurityException) {
            // Permissions not granted
            val message = "Emergency! I need help (via $reason). Location permission denied."
            sendToAllContactsOrFallback(message)
        }
    }

    private fun sendToAllContactsOrFallback(message: String) {
        val numbers = getAllEmergencyNumbersForCurrentUser()
        val targets = if (numbers.isNotEmpty()) numbers else listOf(fallbackNumber)

        var success = 0
        var fail = 0
        targets.forEach { phone ->
            try {
                val clean = phone.trim()
                if (clean.isEmpty()) return@forEach
                val sms = SmsManager.getDefault()
                sms.sendTextMessage(clean, null, message, null, null)
                success++
            } catch (_: Exception) {
                fail++
            }
        }

        val note = when {
            success > 0 && fail == 0 -> "Emergency SMS sent to ${success} contact(s)!"
            success > 0 && fail > 0  -> "Sent to $success contact(s); $fail failed."
            else                     -> "SMS sending failed."
        }
        Toast.makeText(context, note, Toast.LENGTH_LONG).show()
    }

    private fun getAllEmergencyNumbersForCurrentUser(): List<String> {
        // who is logged in?
        val userPhone = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            .getString("USER_PHONE", null) ?: return emptyList()

        val list = mutableListOf<String>()
        val cursor = dbHelper.getContactsForUser(userPhone)
        cursor.use {
            while (it.moveToNext()) {
                val phone = it.getString(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_CONTACT_PHONE)
                )?.trim().orEmpty()
                if (phone.isNotEmpty()) list += phone
            }
        }
        return list.distinct()
    }
}
