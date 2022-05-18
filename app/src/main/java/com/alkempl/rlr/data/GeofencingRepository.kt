package com.alkempl.rlr.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.alkempl.rlr.data.db.AppDatabase
import com.alkempl.rlr.data.db.ObstacleEntity
import com.alkempl.rlr.data.model.scenario.GeofenceEntry
import com.alkempl.rlr.services.LocationManager
import com.google.firebase.auth.FirebaseUser
import java.util.*
import java.util.concurrent.ExecutorService

private const val TAG = "GeofencingRepository"


/**
 */
class GeofencingRepository private constructor(
    private val database: AppDatabase,
    private val myLocationManager: LocationManager,
    private val executor: ExecutorService
) {

    // in-memory cache
    private var landmarkData = ArrayList<GeofenceEntry>()
    private var active_idx = -1

    fun getStored(): ArrayList<GeofenceEntry> {
        return landmarkData
    }

    fun storeGeofence(entry: GeofenceEntry) {
        landmarkData.add(entry)
    }

    fun storeGeofences(entries: List<GeofenceEntry>) {
        landmarkData.addAll(entries)
    }

    fun getActiveIdx(): Int {
        return active_idx
    }

    fun getActiveEntry(): GeofenceEntry {
        return landmarkData[active_idx]
    }

    fun reset() {
        active_idx = -1
        landmarkData.clear()
    }

    fun processNext() {
        active_idx += 1
        myLocationManager.setActiveGeofence(landmarkData[active_idx])
    }

    init {

    }

    companion object {
        @Volatile
        private var INSTANCE: GeofencingRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): GeofencingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GeofencingRepository(
                    AppDatabase.getInstance(context),
                    LocationManager.getInstance(context),
                    executor
                )
                    .also { INSTANCE = it }
            }
        }
    }
}