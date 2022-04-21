package com.alkempl.rlr.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.alkempl.rlr.R
import com.alkempl.rlr.databinding.ActivityMainBinding
import com.alkempl.rlr.services.SoundService
import com.alkempl.rlr.services.MyService
import com.alkempl.rlr.services.LocationService

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), PermissionRequestFragment.Callbacks, LocationUpdateFragment.Callbacks {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout

        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.username_label).text = intent.getStringExtra("displayName")

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = LocationUpdateFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

        }

        val navController = this.findNavController(R.id.fragment_container)
        NavigationUI.setupActionBarWithNavController(this,navController, drawerLayout)

        val navView = binding.navView
        NavigationUI.setupWithNavController(navView, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.fragment_container)
        return NavigationUI.navigateUp(navController, drawerLayout)
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