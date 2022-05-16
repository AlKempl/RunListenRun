package com.alkempl.rlr.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.viewmodel.LocationUpdateViewModel
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.concurrent.Executors
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.alkempl.rlr.GeofenceBroadcastReceiver
import com.alkempl.rlr.utils.GeofencingConstants
import com.alkempl.rlr.utils.GeofencingConstants.GEOFENCE_VERSION
import com.alkempl.rlr.viewmodel.GeofenceViewModel
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.alkempl.rlr.R
import com.alkempl.rlr.utils.hasPermission


class GeofencingService : Service() {

    var geofencingEnabled = false

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): GeofencingService = this@GeofencingService
    }

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var viewModel: GeofenceViewModel

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onBind(arg0: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)

        geofencingClient = LocationServices.getGeofencingClient(this)

        return START_STICKY
    }

    /*
     * Adds a Geofence for the current clue if needed, and removes any existing Geofence. This
     * method should be called after the user has granted the location permission.  If there are
     * no more geofences, we remove the geofence and let the viewmodel know that the ending hint
     * is now "active."
     */
    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue() {
        if (!applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        if (viewModel.geofenceIsActive()) return
        val currentGeofenceIndex = viewModel.nextGeofenceIndex()
        if(currentGeofenceIndex >= GeofencingConstants.NUM_LANDMARKS) {
            removeGeofences()
            viewModel.geofenceActivated()
            return
        }
        val currentGeofenceData = GeofencingConstants.LANDMARK_DATA[currentGeofenceIndex]

        // Build the Geofence Object
        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence.
            .setRequestId(currentGeofenceData.id)
            // Set the circular region of this geofence.
            .setCircularRegion(currentGeofenceData.latLong.latitude,
                currentGeofenceData.latLong.longitude,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            // Set the expiration duration of the geofence. This geofence gets
            // automatically removed after this period of time.
            .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()

        // First, remove any existing geofences that use our pending intent
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            // Regardless of success/failure of the removal, add the new geofence
            addOnCompleteListener {
                // Add the new geofence request with the new geofence
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        // Geofences added.
//                        Toast.makeText(this@HuntMainActivity, getString(R.string.geofences_added) + " " + GEOFENCE_VERSION,
//                            Toast.LENGTH_SHORT)
//                            .show()
                        Log.e("$TAG/ADD", geofence.requestId)
                        // Tell the viewmodel that we've reached the end of the game and
                        // activated the last "geofence" --- by removing the Geofence.
                        viewModel.geofenceActivated()
                    }
                    addOnFailureListener {
                        // Failed to add geofences.
//                        Toast.makeText(this@HuntMainActivity, R.string.geofences_not_added,
//                            Toast.LENGTH_SHORT).show()
                        if ((it.message != null)) {
                            Log.w("$TAG/ADD", it.message!!)
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                Log.d(TAG, getString(R.string.geofences_removed))
                Toast.makeText(applicationContext, R.string.geofences_removed, Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d(TAG, getString(R.string.geofences_not_removed))
            }
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        initializeLocationManager()
        startForeground(NotificationCreator.getNotificationId(),
            NotificationCreator.getNotification(this))
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    private fun initializeLocationManager() {

    }


    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "com.alkempl.rlr.ACTION_GEOFENCE_EVENT"

        private const val TAG = "GEOFENCING"
    }
}