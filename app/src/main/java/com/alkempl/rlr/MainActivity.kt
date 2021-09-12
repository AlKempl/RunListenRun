package com.alkempl.rlr

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.alkempl.rlr.databinding.ActivityMainBinding
import com.birjuvachhani.locus.Locus
import com.google.android.gms.location.LocationRequest
import java.text.DateFormat.getDateTimeInstance
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var locationEnabled = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Locus.configure {
            forceBackgroundUpdates = true // default: false
            enableBackgroundUpdates = true // default: false
            request {
                fastestInterval = 0
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 0
                maxWaitTime = 2
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.switchButton.text = getString(R.string.enable_tracking_label)
        binding.forceButton.text = "Force"

        binding.forceButton.setOnClickListener {
//            if (locationEnabled) {
                Locus.getCurrentLocation(this) { result ->
                    result.location?.let { /* Received location update */
                        binding.editTextTextMultiLine.setText(
                            generateLocationOutput(it)
                        )
                    }
                    result.error?.let { /* Received error! */ }
                }
//            }
        }
        binding.switchButton.setOnClickListener {
            if (locationEnabled) {
                locationEnabled = false;
                binding.switchButton.text = getString(R.string.enable_tracking_label)
                Locus.stopLocationUpdates()
            } else {
                locationEnabled = true;
                binding.switchButton.text = getString(R.string.disable_tracking_label)
                Locus.startLocationUpdates(this) { result ->
                    result.location?.let {
                        binding.editTextTextMultiLine.setText(
                            generateLocationOutput(it)
                        )
                    }
                    result.error?.let { /* Received error! */ }
                }
            }
            // Code here executes on main thread after user presses button
        }
    }

    private fun generateLocationOutput(it: Location): String {
        return "Latitude: ${it.latitude} \r\n" +
                "Longtitude: ${it.longitude} \r\n" +
                "Accuracy: ${it.accuracy} \r\n" +
                "Speed: ${it.speed} \r\n" +
                "Time: ${getDateTime(it.time)} (${it.time}) \r\n" +
                "Altitude: ${it.altitude} \r\n" +
                "Bearing: ${it.bearing} \r\n" +
                "Mock: ${it.isFromMockProvider} \r\n" +
                "Extras: ${it.extras} \r\n" +
                "Provider: ${it.provider} \r\n"
    }

    private fun getDateTime(milliseconds: Long): String {
        val formatter = getDateTimeInstance()
        val cal = Calendar.getInstance()
        cal.timeInMillis = milliseconds
        return formatter.format(cal.time)
    }

}