package com.example.mkat_nur.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import kotlin.math.*

data class QiblaState(
    val compassAngle: Float = 0f,
    val qiblaAngle: Float = 0f,
    val distanceToKaaba: Float = 0f,
    val isLoading: Boolean = true,
    val error: String? = null,
    val sensorAccuracy: Int = 3,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val sunAzimuth: Float? = null
)

class QiblaViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gmsLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _state = MutableStateFlow(QiblaState())
    val state: StateFlow<QiblaState> = _state.asStateFlow()

    private var filteredAccelerometer = FloatArray(3)
    private var filteredMagnetometer = FloatArray(3)
    private val alpha = 0.10f

    private var rotationMatrix = FloatArray(9)
    private var orientationAngles = FloatArray(3)

    private val kaabaLat = 21.422487
    private val kaabaLng = 39.826206

    init {
        startSensorUpdates()
        updateLocationAndQibla()
    }

    private fun startSensorUpdates() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun updateLocationAndQibla() {
        _state.value = _state.value.copy(isLoading = true)
        try {
            @Suppress("MissingPermission")
            gmsLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val qiblaDirection = calculateQiblaDirection(location.latitude, location.longitude)
                        val distance = calculateDistance(location.latitude, location.longitude, kaabaLat, kaabaLng)
                        val sunAz = calculateSunAzimuth()

                        _state.value = _state.value.copy(
                            qiblaAngle = qiblaDirection.toFloat(),
                            distanceToKaaba = distance,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            sunAzimuth = sunAz,
                            isLoading = false
                        )
                    } else {
                        _state.value = _state.value.copy(isLoading = false, error = "Konum alınamadı")
                    }
                }
                .addOnFailureListener {
                    _state.value = _state.value.copy(isLoading = false, error = it.message)
                }
        } catch (e: SecurityException) {
            _state.value = _state.value.copy(isLoading = false, error = "Konum izni eksik")
        }
    }

    private fun calculateSunAzimuth(): Float {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY).toDouble() + cal.get(Calendar.MINUTE).toDouble() / 60.0
        return (((hour - 12.0) * 15.0 + 180.0) % 360.0).toFloat()
    }

    private fun calculateQiblaDirection(lat: Double, lng: Double): Double {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(kaabaLat)
        val deltaLambda = Math.toRadians(kaabaLng - lng)
        val y = sin(deltaLambda)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val qiblaAngle = Math.toDegrees(atan2(y, x))
        return (qiblaAngle + 360) % 360
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                filteredAccelerometer = applyLowPassFilter(event.values, filteredAccelerometer)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                filteredMagnetometer = applyLowPassFilter(event.values, filteredMagnetometer)
            }
        }
        updateOrientationAngles()
    }

    private fun applyLowPassFilter(input: FloatArray, output: FloatArray): FloatArray {
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
        return output
    }

    private fun updateOrientationAngles() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, filteredAccelerometer, filteredMagnetometer)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            
            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            _state.value = _state.value.copy(
                compassAngle = (azimuth + 360) % 360,
                pitch = pitch,
                roll = roll
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            _state.value = _state.value.copy(sensorAccuracy = accuracy)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}
