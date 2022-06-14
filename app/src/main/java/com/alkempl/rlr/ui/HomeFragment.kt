package com.alkempl.rlr.ui

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alkempl.rlr.databinding.FragmentHomeBinding
import com.alkempl.rlr.utils.hasPermission
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResult
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private var activityListener: Callbacks? = null

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        return inflater.inflate(R.layout.fragment_home, container, false)

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.enableBackgroundLocationButton.setOnClickListener {
//            activityListener?.requestPermissionFragment()
        }
        binding.startSession.setOnClickListener {
            activityListener?.sessionStartFragment()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
//        updateBackgroundButtonState()
    }

    private fun showBackgroundButton(): Boolean {
        return requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && !requireContext().hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun updateBackgroundButtonState() {
        if (showBackgroundButton()) {
            binding.enableBackgroundLocationButton.visibility = View.VISIBLE
        } else {
            binding.enableBackgroundLocationButton.visibility = View.GONE
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Callbacks) {
            activityListener = context

            // If fine location permission isn't approved, instructs the parent Activity to replace
            // this fragment with the permission request fragment.
            if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                activityListener?.requestPermissionFragment(PermissionRequestType.FINE_LOCATION)
            }else{
                if (!context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    activityListener?.requestPermissionFragment(PermissionRequestType.BACKGROUND_LOCATION
                    )
                } else {
                    if (!context.hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)) {
                        activityListener?.requestPermissionFragment(PermissionRequestType.ACTIVITY_RECOGNITION)
                    }
                }
            }

        } else {
            throw RuntimeException("$context must implement LocationUpdateFragment.Callbacks")
        }
    }

    override fun onDetach() {
        super.onDetach()
        activityListener = null
    }

    interface Callbacks {
        fun requestPermissionFragment(type: PermissionRequestType)
        fun sessionStartFragment()
    }

}