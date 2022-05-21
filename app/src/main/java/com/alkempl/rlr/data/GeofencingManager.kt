package com.alkempl.rlr.data

import android.content.Context
import android.util.Log
import com.alkempl.rlr.data.db.AppDatabase
import com.alkempl.rlr.data.model.scenario.GeofenceEntry
import com.alkempl.rlr.services.LocationManager
import java.util.*
import java.util.concurrent.ExecutorService

private const val TAG = "GeofencingRepository"


/**
 */
class GeofencingManager private constructor(
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

    fun getActiveEntry(): GeofenceEntry? {
        return landmarkData.getOrNull(active_idx)
    }

    fun reset() {
        Log.d(TAG, "reset")
        active_idx = -1
        landmarkData.clear()
    }

    fun processNext() {
        Log.d(TAG, "processNext")
        active_idx += 1
        if(active_idx > landmarkData.size - 1){
            myLocationManager.removeGeofences()
        }else{
            myLocationManager.setActiveGeofence(landmarkData[active_idx])
        }
    }

    init {

    }

    companion object {
        @Volatile
        private var INSTANCE: GeofencingManager? = null

        fun getInstance(context: Context, executor: ExecutorService): GeofencingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GeofencingManager(
                    AppDatabase.getInstance(context),
                    LocationManager.getInstance(context),
                    executor
                )
                    .also { INSTANCE = it }
            }
        }
    }
}