package com.alkempl.rlr.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alkempl.rlr.R
import com.alkempl.rlr.adapter.MyItemRecyclerViewAdapter
import com.alkempl.rlr.databinding.FragmentItemListBinding
import com.alkempl.rlr.databinding.FragmentLocationUpdateBinding
import com.alkempl.rlr.hasPermission
import com.alkempl.rlr.services.LocationService
import com.alkempl.rlr.viewmodel.LocationUpdateViewModel
import java.lang.StringBuilder

private const val TAG = "LocationUpdateFragment"

class LocationUpdateFragment : Fragment() {

    private var activityListener: Callbacks? = null

    private lateinit var binding: FragmentLocationUpdateBinding
    private lateinit var bindingItemList: FragmentItemListBinding

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

        binding.profileButton.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_locationUpdateFragment_to_profileFragment)
        }
/*
        bindingRecyclerViewAdapter.list.adapter = MyItemRecyclerViewAdapter(locationUpdateViewModel.locationListLiveData.value!!);
*/

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
                    det.adapter = MyItemRecyclerViewAdapter(locations);
                }
            }
        )


        locationUpdateViewModel.receivingLocationUpdates.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { receivingLocation ->
                updateStartOrStopButtonState(receivingLocation)
            }
        )

        locationUpdateViewModel.locationListLiveData.observe(
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
                        /* bindingRecyclerViewAdapter.list.adapter.updateUserList(locations) */
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundButtonState()
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

    private fun updateStartOrStopButtonState(receivingLocation: Boolean) {
        if (receivingLocation) {
            binding.startOrStopLocationUpdatesButton.apply {
                text = getString(R.string.stop_receiving_location)
                setOnClickListener {
                    context?.stopService(Intent(context, LocationService::class.java))
                }
            }
        } else {
            binding.startOrStopLocationUpdatesButton.apply {
                text = getString(R.string.start_receiving_location)
                setOnClickListener {
                    context?.startService(Intent(context, LocationService::class.java))
                }
            }
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