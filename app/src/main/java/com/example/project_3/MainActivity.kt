package com.example.project_3

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project_3.ui.theme.Project_3Theme
import kotlin.math.min
import kotlin.time.measureTime

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
fun MotionDetectionScreen(paddingValues: PaddingValues){
    //to access hardware device of andriod
    val context = LocalContext.current

    //trigger sensorManager with the help of above context
    // type cast it as sensorManager
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // variable to count number of steps ? for null or not for the number of steps

    val stepCounterSensor: Sensor? =sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    // saves the number of steps taken
    var stepCount by remember { mutableStateOf(0f) }

    // stores current activity
    var currentActivity by remember { mutableStateOf("Idle") }

    // var to know when a particular activity starts the timing
    var activityStartTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // var to save previous number of steps and last time stamp taken before any activity starts(new one)
    var lastStepCount  by remember { mutableStateOf(0f) }
    var lastTimestamp by remember { mutableStateOf(0L) }

    DisposableEffect(stepCounterSensor) {
        // a variable for when a sensor is triggerd
        val sensorListener = object : SensorEventListener{
            override fun onSensorChanged(event: SensorEvent?) {
                //sensorChange is basically when a method is called for any change in the sensor
                // or when the sensor has been triggered
                event?.let{
                    val newSteps = it.values[0]
                    val currentTime = System.currentTimeMillis()

                    stepCount = newSteps

                    if(lastTimestamp != 0L){
                        val deltaSteps = newSteps-lastStepCount
                        val deltaTimeSec = (currentTime-lastTimestamp)/1000

                        if(deltaTimeSec>0){
                            val cadence = deltaSteps/deltaTimeSec
                            val newStatus = when{
                                cadence.toDouble() == 0.0 -> "Idle"
                                cadence > 2.5 -> "Runnig"
                                cadence > 5 -> "Vehicle"
                                else -> "Walking"
                            }
                            if(newStatus!=currentActivity){
                                // if there is change in activity
                                val durationMillis = currentTime - activityStartTime
                                val totalSeconds = (durationMillis)/1000.toInt() //converts milli sec to sec
                                //convert to mins and sec
                                val minutes = totalSeconds/60
                                val seconds = totalSeconds % 60
                                val durationString = if(minutes > 0){
                                    "$minutes min,$seconds sec "
                                }else{
                                    "$seconds sec"
                                }

                                if(activityStartTime !=currentTime){
                                    Toast.makeText(context,
                                        "You have just ${currentActivity.lowercase()} for $durationString",
                                        Toast.LENGTH_SHORT).show()
                                }

                                currentActivity = newStatus
                                activityStartTime = currentTime

                            }
                        }

                    }

                    lastStepCount = newSteps
                    lastTimestamp = currentTime
                    stepCount = newSteps
                }
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                TODO("Not yet implemented")
            }

        }

        stepCounterSensor?.let{
            // register sensor
            sensorManager.registerListener(sensorListener, it,SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose {
            // unregister sensor
            sensorManager.unregisterListener(sensorListener)
        }
    }
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "Steps taken since app started: ${stepCount.toInt()}", fontSize = 25.sp
        )

        when(currentActivity){
            "Idle" ->{
                Image(
                    painter = painterResource(id = R.drawable.still),
                    contentDescription = "Idle",
                    modifier = Modifier.height(600.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "You are $currentActivity",
                    fontSize = 25.sp
                )
            }

            "Walking" ->{
                Image(
                    painter = painterResource(id = R.drawable.running),
                    contentDescription = "Running",
                    modifier = Modifier.height(600.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "You are $currentActivity",
                    fontSize = 25.sp
                )
            }

            "Walking" ->{
                Image(
                    painter = painterResource(id = R.drawable.walking),
                    contentDescription = "Walking",
                    modifier = Modifier.height(600.dp),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "You are $currentActivity",
                    fontSize = 25.sp
                )
            }
        }
    }

}

