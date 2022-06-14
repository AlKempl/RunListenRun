package com.alkempl.rlr.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alkempl.rlr.BuildConfig
import com.alkempl.rlr.R
import com.alkempl.rlr.databinding.FragmentPermissionRequestBinding
import com.alkempl.rlr.utils.hasPermission
import com.alkempl.rlr.utils.requestPermissionWithRationale
import com.google.android.material.snackbar.Snackbar

private const val TAG = "PermissionRequestFrag"

class PermissionRequestFragment : Fragment() {
    // Type of permission to request (fine or background). Set by calling Activity.
    private var permissionRequestType: PermissionRequestType? = null

    private lateinit var binding: FragmentPermissionRequestBinding

    private var activityListener: Callbacks? = null

    // If the user denied a previous permission request, but didn't check "Don't ask again", these
    // Snackbars provided an explanation for why user should approve, i.e., the additional
    // rationale.
    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
            binding.frameLayout,
            R.string.fine_location_permission_rationale,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
    }

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
            binding.frameLayout,
            R.string.background_location_permission_rationale,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
    }

    private val activityRecognitionRationalSnackbar by lazy {
        Snackbar.make(
            binding.frameLayout,
            R.string.activity_recognition_permission_denied_explanation,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSIONS_REQUEST_CODE
                )
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Callbacks) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement PermissionRequestFragment.Callbacks")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionRequestType =
            arguments?.getSerializable(ARG_PERMISSION_REQUEST_TYPE) as PermissionRequestType
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPermissionRequestBinding.inflate(inflater, container, false)

        @Suppress("NON_EXHAUSTIVE_WHEN_STATEMENT")
        when (permissionRequestType) {
            PermissionRequestType.FINE_LOCATION -> {

                binding.apply {
                    iconImageView.setImageResource(R.drawable.ic_location_on_24px)

                    titleTextView.text =
                        getString(R.string.fine_location_access_rationale_title_text)

                    detailsTextView.text =
                        getString(R.string.fine_location_access_rationale_details_text)

                    permissionRequestButton.text =
                        getString(R.string.enable_fine_location_button_text)
                }
            }

            PermissionRequestType.BACKGROUND_LOCATION -> {

                binding.apply {
                    iconImageView.setImageResource(R.drawable.ic_my_location_24px)

                    titleTextView.text =
                        getString(R.string.background_location_access_rationale_title_text)

                    detailsTextView.text =
                        getString(R.string.background_location_access_rationale_details_text)

                    permissionRequestButton.text =
                        getString(R.string.enable_background_location_button_text)
                }
            }

            PermissionRequestType.ACTIVITY_RECOGNITION -> {

                binding.apply {
                    iconImageView.setImageResource(R.drawable.ic_baseline_directions_run_24)

                    titleTextView.text =
                        getString(R.string.activity_recognition_access_rationale_title_text)

                    detailsTextView.text =
                        getString(R.string.activity_recognition_access_rationale_details_text)

                    permissionRequestButton.text =
                        getString(R.string.enable_activity_recognition_button_text)
                }
            }
        }

        binding.permissionRequestButton.setOnClickListener {
            @Suppress("NON_EXHAUSTIVE_WHEN_STATEMENT")
            when (permissionRequestType) {
                PermissionRequestType.FINE_LOCATION ->
                    requestFineLocationPermission()

                PermissionRequestType.BACKGROUND_LOCATION ->
                    requestBackgroundLocationPermission()

                PermissionRequestType.ACTIVITY_RECOGNITION ->
                    requestActivityRecognitionPermission()
            }
        }

        return binding.root
    }

    override fun onDetach() {
        super.onDetach()

        activityListener = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
            REQUEST_ACTIVITY_RECOGNITION_PERMISSIONS_REQUEST_CODE,
            REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive an empty array.
                    Log.d(TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                {
                    when(requestCode){
                        REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE -> {
                            activityListener?.requestPermissionFragment(PermissionRequestType.BACKGROUND_LOCATION)
                        }
                        REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE -> {
                            activityListener?.requestPermissionFragment(PermissionRequestType.ACTIVITY_RECOGNITION)
                        }
                        REQUEST_ACTIVITY_RECOGNITION_PERMISSIONS_REQUEST_CODE -> {
                            activityListener?.displayHomeUI()
                        }
                    }
                }

                else -> {

                    val permissionDeniedExplanation =
                        if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
                            R.string.fine_permission_denied_explanation
                        } else {
                            R.string.background_permission_denied_explanation
                        }

                    Snackbar.make(
                        binding.frameLayout,
                        permissionDeniedExplanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    private fun requestFineLocationPermission() {
        val permissionApproved =
            context?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ?: return

        if (permissionApproved) {
            activityListener?.requestPermissionFragment(PermissionRequestType.BACKGROUND_LOCATION)
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
            )
        }
    }

    private fun requestBackgroundLocationPermission() {
        val permissionApproved =
            context?.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ?: return

        if (permissionApproved) {
            activityListener?.requestPermissionFragment(PermissionRequestType.ACTIVITY_RECOGNITION)
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
            )
        }
    }

    private fun requestActivityRecognitionPermission() {
        val permissionApproved =
            context?.hasPermission(Manifest.permission.ACTIVITY_RECOGNITION) ?: return

        if (permissionApproved) {
            activityListener?.displayHomeUI()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_ACTIVITY_RECOGNITION_PERMISSIONS_REQUEST_CODE,
            )
        }
    }

    companion object {
        private const val ARG_PERMISSION_REQUEST_TYPE = "com.alkempl.rlr.PERMISSION_REQUEST_TYPE"

        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56
        private const val REQUEST_ACTIVITY_RECOGNITION_PERMISSIONS_REQUEST_CODE = 78

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param permissionRequestType Type of permission you would like to request.
         * @return A new instance of fragment PermissionRequestFragment.
         */
        @JvmStatic
        fun newInstance(permissionRequestType: PermissionRequestType) =
            PermissionRequestFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PERMISSION_REQUEST_TYPE, permissionRequestType)
                }
            }
    }

    interface Callbacks {
        fun displayHomeUI()
        fun requestPermissionFragment(type: PermissionRequestType)
    }
}

enum class PermissionRequestType {
    FINE_LOCATION, BACKGROUND_LOCATION, ACTIVITY_RECOGNITION
}
