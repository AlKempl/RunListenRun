package com.alkempl.rlr.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.alkempl.rlr.R
import com.alkempl.rlr.adapter.MyItemRecyclerViewAdapter
import com.alkempl.rlr.databinding.FragmentItemListBinding
import com.alkempl.rlr.databinding.FragmentLocationUpdateBinding
import com.alkempl.rlr.hasPermission
import com.alkempl.rlr.services.LocationService
import com.alkempl.rlr.services.MyService
import com.alkempl.rlr.services.ScenarioService
import com.alkempl.rlr.services.SoundService
import com.alkempl.rlr.viewmodel.LocationUpdateViewModel
import java.lang.StringBuilder

private const val TAG = "LocationUpdateFragment"

class LocationUpdateFragment : Fragment() {

    private var activityListener: Callbacks? = null

    private lateinit var binding: FragmentLocationUpdateBinding
    private lateinit var bindingItemList: FragmentItemListBinding

    private var scenarioService: ScenarioService? = null
    private var scenarioServiceBounded = false
    private val scenarioServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as ScenarioService.LocalBinder
            scenarioService = binder.getService()
            scenarioServiceBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            scenarioServiceBounded = false
            scenarioService = null
        }
    }

    private val locationUpdateViewModel by lazy {
        ViewModelProviders.of(this).get(LocationUpdateViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationUpdateBinding.inflate(inflater, container, false)
        bindingItemList = FragmentItemListBinding.inflate(inflater, container, false)

        binding.enableBackgroundLocationButton.setOnClickListener {
            activityListener?.requestBackgroundLocationPermission()
        }

        binding.scenarioControlButton.setOnClickListener {
            val scenarioServiceIntent = Intent(context, ScenarioService::class.java)

            if (scenarioServiceBounded && scenarioService!!.isRunning()) {
                Log.d(TAG, "stop Scenario Service")
                requireActivity().unbindService(scenarioServiceConnection)
                requireActivity().stopService(scenarioServiceIntent)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d(TAG, "start Scenario ForegroundService")
                    requireActivity().startForegroundService(scenarioServiceIntent)
                } else {
                    Log.d(TAG, "start Scenario Service")
                    requireActivity().startService(scenarioServiceIntent)
                }
                // Bind to LocalService
                Intent(context, ScenarioService::class.java).also { ssintent ->
                    requireActivity().bindService(
                        ssintent,
                        scenarioServiceConnection,
                        Context.BIND_AUTO_CREATE
                    )
                }
            }
            updateScenarioButtonState()
        }


        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Callbacks) {
            activityListener = context

            // If fine location permission isn't approved, instructs the parent Activity to replace
            // this fragment with the permission request fragment.
            if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                activityListener?.requestFineLocationPermission()
            }
        } else {
            throw RuntimeException("$context must implement LocationUpdateFragment.Callbacks")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationUpdateViewModel.locationListLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { locations ->
                locations?.let {
                    val det: RecyclerView = binding.fragmentContainerView.findViewById(R.id.list)
                    with(det.adapter!!) {
                        if (this is MyItemRecyclerViewAdapter) {
                            updateUserList(locations)
                        }
                    }
                }
            }
        )


        /*locationUpdateViewModel.locationListLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { locations ->
                locations?.let {
                    Log.d(TAG, "Got ${locations.size} locations")

                    if (locations.isEmpty()) {
                        binding.locationOutputTextView.text =
                            getString(R.string.emptyLocationDatabaseMessage)
                    } else {
                        val outputStringBuilder = StringBuilder("")
                        for (location in locations) {
                            outputStringBuilder.append(location.toString() + "\n")
                        }

                        binding.locationOutputTextView.text = outputStringBuilder.toString()
                        *//* bindingRecyclerViewAdapter.list.adapter.updateUserList(locations) *//*
                    }
                }
            }
        )*/
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundButtonState()
        updateScenarioButtonState()
    }

    override fun onPause() {
        super.onPause()

        // Stops location updates if background permissions aren't approved. The FusedLocationClient
        // won't trigger any PendingIntents with location updates anyway if you don't have the
        // background permission approved, but it's best practice to unsubscribing anyway.
        // To simplify the sample, we are unsubscribing from updates here in the Fragment, but you
        // could do it at the Activity level if you want to continue receiving location updates
        // while the user is moving between Fragments.
//        if ((locationUpdateViewModel.receivingLocationUpdates.value == true) &&
//            (!requireContext().hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
//            locationUpdateViewModel.stopLocationUpdates()
//        }
    }

    override fun onDetach() {
        super.onDetach()
        activityListener = null
        requireActivity().unbindService(scenarioServiceConnection)
    }

    private fun showBackgroundButton(): Boolean {
        return !requireContext().hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun updateBackgroundButtonState() {
        if (showBackgroundButton()) {
            binding.enableBackgroundLocationButton.visibility = View.VISIBLE
        } else {
            binding.enableBackgroundLocationButton.visibility = View.GONE
        }
    }

    private fun updateScenarioButtonState() {
        if (scenarioServiceBounded && scenarioService!!.isRunning()) {
            binding.scenarioControlButton.text = "Stop scenario"
        } else {
            binding.scenarioControlButton.text = "Start scenario"
        }
    }


    companion object {
        fun newInstance() = LocationUpdateFragment()
    }

    interface Callbacks {
        fun requestFineLocationPermission()
        fun requestBackgroundLocationPermission()
    }
}