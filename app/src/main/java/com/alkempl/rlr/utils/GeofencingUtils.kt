package com.alkempl.rlr.utils

import android.content.Context
import com.alkempl.rlr.BuildConfig
import com.alkempl.rlr.R
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.TimeUnit

/**
 * Returns the error string for a geofencing error code.
 */
fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.unknown_geofence_error)
    }
}

/**
 * Stores latitude and longitude information along with a hint to help user find the location.
 */
data class LandmarkDataObject(val id: String, val hint: Int, val name: Int, val latLong: LatLng)

internal object GeofencingConstants {

    /**
     * Used to set an expiration time for a geofence. After this amount of time, Location services
     * stops tracking the geofence. For this sample, geofences expire after one hour.
     */
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    val LANDMARK_DATA = arrayOf(
//        LandmarkDataObject(
//            "golden_gate_bridge",
//            R.string.golden_gate_bridge_hint,
//            R.string.golden_gate_bridge_location,
//            LatLng(37.819927, -122.478256)),
//
//        LandmarkDataObject(
//            "ferry_building",
//            R.string.ferry_building_hint,
//            R.string.ferry_building_location,
//            LatLng(37.795490, -122.394276)),
//
//        LandmarkDataObject(
//            "pier_39",
//            R.string.pier_39_hint,
//            R.string.pier_39_location,
//            LatLng(37.808674, -122.409821)),
//
//        LandmarkDataObject(
//           "union_square",
//            R.string.union_square_hint,
//            R.string.union_square_location,
//            LatLng(37.788151, -122.407570))
        LandmarkDataObject(
            "garden",
            R.string.garden_hint,
            R.string.garden_location,
            LatLng(47.260840, 39.648080)),
//    LandmarkDataObject(
//           "atelier",
//            R.string.atelier_hint,
//            R.string.atelier_location,
//            LatLng(47.260269, 39.649627)),
//    LandmarkDataObject(
//           "magnit",
//            R.string.magnit_hint,
//            R.string.magnit_location,
//            LatLng(47.260529, 39.650953)),
        LandmarkDataObject(
            "kindergarten",
            R.string.kindergarten_hint,
            R.string.kindergarten_location,
            LatLng(47.260900, 39.650110)),
        LandmarkDataObject(
            "bench",
            R.string.bench_hint,
            R.string.bench_location,
            LatLng(47.261060, 39.648680))
    )

    val NUM_LANDMARKS = LANDMARK_DATA.size
    const val GEOFENCE_RADIUS_IN_METERS = 20f
    const val GEOFENCE_VERSION = "v3 ${GEOFENCE_RADIUS_IN_METERS}f ${BuildConfig.VERSION_NAME}"
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
}
