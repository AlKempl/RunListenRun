package com.alkempl.rlr

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.alkempl.rlr.databinding.ActivityMainBinding
import com.birjuvachhani.locus.Locus
import com.google.android.gms.location.*
import java.text.DateFormat.getDateTimeInstance
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var locationEnabled = false
    private lateinit var binding: ActivityMainBinding

    // declare a global variable FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // in onCreate() initialize FusedLocationProviderClient


    // globally declare LocationRequest
    private lateinit var locationRequest: LocationRequest

    // globally declare LocationCallback
    private lateinit var locationCallback: LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Locus.configure {
//            forceBackgroundUpdates = true // default: false
//            enableBackgroundUpdates = true // default: false
//            request {
//                fastestInterval = 0
//                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//                interval = 0
//                maxWaitTime = 2
//            }
//        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        getLocationUpdates()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.switchButton.text = getString(R.string.enable_tracking_label)
        binding.forceButton.text = "Force"

        binding.forceButton.setOnClickListener {
//            if (locationEnabled) {
            Locus.getCurrentLocation(this) { result ->
                result.location?.let { /* Received location update */
                        setLocationOutput(it)
                }
                result.error?.let { /* Received error! */ }
            }
//            }
        }
        binding.switchButton.setOnClickListener {
            if (locationEnabled) {
                locationEnabled = false;
                binding.switchButton.text = getString(R.string.enable_tracking_label)
//                Locus.stopLocationUpdates()
                stopLocationUpdates()
            } else {
                locationEnabled = true;
                binding.switchButton.text = getString(R.string.disable_tracking_label)
//                Locus.startLocationUpdates(this) { result ->
//                    result.location?.let {
//                        binding.editTextTextMultiLine.setText(
//                            generateLocationOutput(it)
//                        )
//                    }
//                    result.error?.let { /* Received error! */ }
//                }
                startLocationUpdates()
            }
            // Code here executes on main thread after user presses button
        }
    }

    private fun setLocationOutput(it: Location) {
        binding.latValue.text = it.latitude.toString()
        binding.lonValue.text = it.longitude.toString()
        binding.accValue.text = it.accuracy.toString()
        binding.speedValue.text = it.speed.toString()
        binding.timeValue.setText(getDateTime(it.time) + " ("+ it.time + ")")
        binding.altValue.text = it.altitude.toString()
        binding.bearValue.text = it.bearing.toString()
        binding.mockValue.text = it.isFromMockProvider.toString()
        binding.srcValue.text = it.provider.toString()
        binding.editTextTextMultiLine.setText(it.extras.toString())
    }

    private fun getDateTime(milliseconds: Long): String {
        val formatter = getDateTimeInstance()
        val cal = Calendar.getInstance()
        cal.timeInMillis = milliseconds
        return formatter.format(cal.time)
    }

    /**
     * call this method in onCreate
     * onLocationResult call when location is changed
     */
    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        locationRequest = LocationRequest()
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
//        locationRequest.smallestDisplacement = 0 // 170 m = 0.1 mile
        locationRequest.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    // get latest location
                    val location =
                        locationResult.lastLocation
                    // use your location object
                    // get latitude , longitude and other info from this
                    setLocationOutput(location)
                }
            }
        }
    }

    //start location updates
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // stop receiving location update when activity not visible/foreground
    override fun onPause() {
        super.onPause()
        if (locationEnabled) {
            stopLocationUpdates()
        }
    }

    // start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        if (locationEnabled) {
            startLocationUpdates()
        }
    }

    fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                if (location != null) {
                    // use your location object
                    // get latitude , longitude and other info from this
                    setLocationOutput(location)
                }

            }

    }

}