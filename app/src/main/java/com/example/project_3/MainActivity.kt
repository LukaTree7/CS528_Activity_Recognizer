package com.example.project_3

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project_3.ui.theme.Project_3Theme
import com.google.android.gms.maps.model.LatLng
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            Project_3Theme {
                MotionDetectionScreen(PaddingValues(10.dp))
            }
        }
    }
}

@Composable
fun MotionDetectionScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    var stepCount by remember { mutableStateOf(0f) }
    var visitCC by remember { mutableStateOf(0f) }
    var visitUH by remember { mutableStateOf(0f) }
    var currentActivity by remember { mutableStateOf("Still") }
    var activityStartTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var lastStepCount by remember { mutableStateOf(0f) }
    var lastTimestamp by remember { mutableStateOf(0L) }

    if (stepCounterSensor == null) {
        Toast.makeText(context, "Step counter sensor not available!", Toast.LENGTH_SHORT).show()
    }

    DisposableEffect(stepCounterSensor) {
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val newSteps = it.values[0]
                    val currentTime = System.currentTimeMillis()

                    stepCount = newSteps
                    // Check if steps have increased
                    val deltaSteps = newSteps - lastStepCount
                    val deltaTimeSec = (currentTime - lastTimestamp) / 1000.0  // Convert to seconds

//                    if (lastTimestamp != 0L) {
//                        val deltaSteps = newSteps - lastStepCount
//                        val deltaTimeSec = (currentTime - lastTimestamp) / 1000

                        if (deltaTimeSec > 0) {
                            val cadence = deltaSteps / deltaTimeSec
                            val newStatus = when {
                                cadence.toDouble() == 0.0 -> "Still"
                                cadence > 2.5 -> "Running"
                                cadence > 5 -> "In Vehicle"
                                else -> "Walking"
                            }
                            if (newStatus != currentActivity) {
                                val durationMillis = currentTime - activityStartTime
                                val totalSeconds = (durationMillis / 1000).toInt()
                                val minutes = totalSeconds / 60
                                val seconds = totalSeconds % 60
                                val durationString = if (minutes > 0) {
                                    "$minutes min, $seconds sec"
                                } else {
                                    "$seconds sec"
                                }

                                if (activityStartTime != currentTime) {
                                    Toast.makeText(
                                        context,
                                        "You have were  ${currentActivity.lowercase()} for $durationString",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                currentActivity = newStatus
                                activityStartTime = currentTime
                            }
                        }


                    lastStepCount = newSteps
                    lastTimestamp = currentTime
                    stepCount = newSteps
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d("Sensor", "Accuracy changed: $accuracy")
            }
        }

        stepCounterSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Visit to Campus Center geoFence: ${visitCC.toInt()}", fontSize = 25.sp
        )
        Text(
            text = "Visit to Unit Hall geoFence: ${visitUH.toInt()}", fontSize = 25.sp
        )
        Text(
            text = "Steps taken since app started: ${stepCount.toInt()}", fontSize = 25.sp
        )

        Box(
            modifier = Modifier.height(250.dp)
        ) {
            MapView()
        }

        when (currentActivity) {
            "Still" -> {
                Image(
                    painter = painterResource(id = R.drawable.still),
                    contentDescription = "Still",
                    modifier = Modifier
                        .size(250.dp)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "You are $currentActivity",
                    fontSize = 25.sp
                )
            }
            "Running" -> {
                Image(
                    painter = painterResource(id = R.drawable.running),
                    contentDescription = "Running",
                    modifier = Modifier
                        .size(250.dp)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "You are $currentActivity.",
                    fontSize = 25.sp
                )
            }
            "Walking" -> {
                Image(
                    painter = painterResource(id = R.drawable.walking),
                    contentDescription = "Walking",
                    modifier = Modifier
                        .size(250.dp)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "You are $currentActivity.",
                    fontSize = 25.sp
                )
            }
            "In Vehicle" -> {
                Image(
                    painter = painterResource(id = R.drawable.driving),
                    contentDescription = "In Vehicle",
                    modifier = Modifier
                        .size(250.dp)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "You are $currentActivity.",
                    fontSize = 25.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapView() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val locationPermissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val defaultLocation = LatLng(42.2707, -71.8044)

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            val locationRequest = LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        currentLocation = LatLng(location.latitude, location.longitude)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

//            onDispose {
//                fusedLocationClient.removeLocationUpdates(locationCallback)
//            }
        } else {
            currentLocation = defaultLocation
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation ?: defaultLocation, 15f)
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .height(250.dp)
    ) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState
        ) {
            currentLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = if (location == defaultLocation) "Default Location" else "Your Location",
                    snippet = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                )
            }
        }
    }
}