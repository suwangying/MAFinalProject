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
    private val threshold: Float = 25f,   // same as your original
    private val debounceMs: Long = 500L,  // ignore rapid repeats
    private val onShake: () -> Unit
) {
    private val appContext = context.applicationContext
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastTrigger = 0L

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val accel = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            val now = SystemClock.elapsedRealtime()
            if (accel > threshold && now - lastTrigger > debounceMs) {
                lastTrigger = now
                Toast.makeText(appContext, "Emergency! Shake detected!", Toast.LENGTH_SHORT).show()
                onShake()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
    }
}