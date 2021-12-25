package com.alkempl.rlr.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.alkempl.rlr.R
import com.alkempl.rlr.databinding.ActivityMainBinding
import com.alkempl.rlr.services.MyService
import com.alkempl.rlr.services.LocationService

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), PermissionRequestFragment.Callbacks, LocationUpdateFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = LocationUpdateFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

        }

        val serviceIntent = Intent(applicationContext, LocationService::class.java)
        val serviceIntent2 = Intent(applicationContext, MyService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "startForegroundService")
            applicationContext.startForegroundService(serviceIntent)
            applicationContext.startForegroundService(serviceIntent2)
        } else {
            Log.d(TAG, "startService")

            applicationContext.startService(serviceIntent)
            applicationContext.startService(serviceIntent2)
        }
    }

    // Triggered from the permission Fragment that it's the app has permissions to display the
    // location fragment.
    override fun displayLocationUI() {

        val fragment = LocationUpdateFragment.newInstance()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Triggers a splash screen (fragment) to help users decide if they want to approve the missing
    // fine location permission.
    override fun requestFineLocationPermission() {
        val fragment = PermissionRequestFragment.newInstance(PermissionRequestType.FINE_LOCATION)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Triggers a splash screen (fragment) to help users decide if they want to approve the missing
    // background location permission.
    override fun requestBackgroundLocationPermission() {
        val fragment = PermissionRequestFragment.newInstance(
            PermissionRequestType.BACKGROUND_LOCATION
        )

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}