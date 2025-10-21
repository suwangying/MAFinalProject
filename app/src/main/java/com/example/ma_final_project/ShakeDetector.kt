package com.example.ma_final_project

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val threshold: Float = 15f,   // tweak if you need
    private val debounceMs: Long = 500L,  // ignore rapid repeats
    private val onShake: () -> Unit
) {
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastTrigger = 0L

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent) {
            val (x, y, z) = event.values
            val accel = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val now = SystemClock.elapsedRealtime()
            if (accel > threshold && now - lastTrigger > debounceMs) {
                lastTrigger = now
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
