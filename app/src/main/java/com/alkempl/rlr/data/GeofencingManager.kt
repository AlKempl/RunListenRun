package com.alkempl.rlr.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alkempl.rlr.data.db.AppDatabase
import com.alkempl.rlr.data.model.scenario.GeofenceEntry
import com.alkempl.rlr.services.LocationManager
import java.util.*
import java.util.concurrent.ExecutorService

private const val TAG = "GeofencingManager"


/**
 */
class GeofencingManager private constructor(
    private val database: AppDatabase,
    private val myLocationManager: LocationManager,
    private val executor: ExecutorService
) {

    // in-memory cache
    private var landmarkData = ArrayList<GeofenceEntry>()
    private val _activeIdx = MutableLiveData<Int>(-1)
    private val _maxIdx = MutableLiveData<Int>(0)
    private val _statusHint = MutableLiveData<String>()

    val activeIdx: LiveData<Int>
        get() = _activeIdx

    val maxIdx: LiveData<Int>
        get() = _maxIdx

    val statusHint: LiveData<String>
        get() = _statusHint


    fun setStatusHint(str: String){
        _statusHint.value = str
    }

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
        return _activeIdx.value!!
    }

    fun getActiveEntry(): GeofenceEntry? {
        return landmarkData.getOrNull(_activeIdx.value!!)
    }

    fun reset() {
        Log.d(TAG, "reset")
        _activeIdx.value = -1
        _maxIdx.value = 0
        landmarkData.clear()
        _statusHint.value = ""
        myLocationManager.removeGeofences()
    }

    fun processNext() {
        Log.d(TAG, "processNext")
        _maxIdx.value = landmarkData.size
        _activeIdx.value = _activeIdx.value!! + 1
        if(_activeIdx.value!! > landmarkData.size - 1){
//            myLocationManager.removeGeofences()
        }else{
            myLocationManager.setActiveGeofence(landmarkData[_activeIdx.value!!])
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