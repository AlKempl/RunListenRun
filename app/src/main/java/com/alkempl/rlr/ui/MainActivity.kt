package com.alkempl.rlr.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.alkempl.rlr.R
import com.alkempl.rlr.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(),
    PermissionRequestFragment.Callbacks,
    HomeFragment.Callbacks,
    LocationUpdateFragment.Callbacks {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout

        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.username_label).text =
            intent.getStringExtra("displayName")

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = HomeFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

        val navController = this.findNavController(R.id.fragment_container)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        val navView = binding.navView
        NavigationUI.setupWithNavController(navView, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.fragment_container)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    // Triggered from the permission Fragment that it's the app has permissions to display the
    // location fragment.
    override fun displayHomeUI() {

        val fragment = HomeFragment.newInstance()
//        val fragment = LocationUpdateFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun requestPermissionFragment(type: PermissionRequestType) {
        val fragment = PermissionRequestFragment.newInstance(
            type
        )

        this.supportActionBar?.show()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun sessionStartFragment() {
        val fragment = LocationUpdateFragment.newInstance()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}