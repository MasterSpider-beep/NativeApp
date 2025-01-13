package com.example.nativeapp.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class ShakeDetector(context:Context, private val onShake: () -> Unit ) :SensorEventListener {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTime: Long = 0
    private val shakeThreshold = 12f
    private val shakeTimeout = 500

    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return



        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate the acceleration
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        if (acceleration > shakeThreshold) {
            Log.i("Shake Detector", "Detected shake with acceleration = $acceleration")
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShakeTime > shakeTimeout) {
                lastShakeTime = currentTime
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}