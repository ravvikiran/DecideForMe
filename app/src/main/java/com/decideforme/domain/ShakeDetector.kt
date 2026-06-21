package com.decideforme.domain

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects shake gestures using the device accelerometer.
 * Uses a threshold-based approach with debouncing.
 */
class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var lastShakeTime: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var lastUpdate: Long = 0

    companion object {
        private const val SHAKE_THRESHOLD = 12.0f
        private const val SHAKE_COOLDOWN_MS = 1500L
        private const val UPDATE_INTERVAL_MS = 100L
    }

    fun start() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastUpdate) < UPDATE_INTERVAL_MS) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (lastUpdate != 0L) {
            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ

            val acceleration = sqrt(
                (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()
            ).toFloat()

            if (acceleration > SHAKE_THRESHOLD) {
                if ((currentTime - lastShakeTime) > SHAKE_COOLDOWN_MS) {
                    lastShakeTime = currentTime
                    onShake()
                }
            }
        }

        lastX = x
        lastY = y
        lastZ = z
        lastUpdate = currentTime
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}
