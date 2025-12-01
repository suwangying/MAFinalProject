package com.example.ma_final_project

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.widget.Toast
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val threshold: Float = 25f,   // how strong the shake must be

    private val debounceMs: Long = 500L,  
    private val onShake: () -> Unit
) {
    private val appContext = context.applicationContext
    // Access the phone's accelerometer through SensorManager
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastTrigger = 0L

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            // Calculate magnitude of acceleration vector
            val accel = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            val now = SystemClock.elapsedRealtime()
            // If acceleration is above threshold AND enough time has passed since last trigger
            if (accel > threshold && now - lastTrigger > debounceMs) {
                lastTrigger = now
                // Quick toast so user knows the shake gesture worked
                Toast.makeText(appContext, "Emergency! Shake detected!", Toast.LENGTH_SHORT).show()
                onShake()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    // Begin listening to accelerometer data
    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
    }
}
