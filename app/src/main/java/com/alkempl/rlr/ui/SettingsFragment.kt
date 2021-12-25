package com.alkempl.rlr.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.preference.SwitchPreferenceCompat
import com.alkempl.rlr.R
import com.alkempl.rlr.services.LocationService
import com.alkempl.rlr.viewmodel.LocationUpdateViewModel

class SettingsFragment : PreferenceFragmentCompat() {
    private val locationUpdateViewModel by lazy {
        ViewModelProviders.of(this).get(LocationUpdateViewModel::class.java)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationUpdateViewModel.receivingLocationUpdates.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { receivingLocation ->
                updateStartOrStopButtonState(receivingLocation)
            }
        )
    }

    private fun updateStartOrStopButtonState(receivingLocation: Boolean) {
        val backgroundLocationUpdatesSwitchPref: SwitchPreference? = findPreference("bg_location_updates")

            backgroundLocationUpdatesSwitchPref?.apply {
                isChecked = receivingLocation
                setOnPreferenceChangeListener { preference, newValue -> logIfUserDisabledFeature(preference, newValue) }

            }
    }

    private fun logIfUserDisabledFeature(pref: Preference?, newValue: Any): Boolean {
        if (newValue as Boolean) {
            locationUpdateViewModel.startLocationUpdates()
        }else{
            locationUpdateViewModel.stopLocationUpdates()
        }
        return true
    }


}