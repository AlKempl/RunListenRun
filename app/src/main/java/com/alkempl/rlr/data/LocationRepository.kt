package com.alkempl.rlr.data

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.alkempl.rlr.data.db.AppDatabase
import com.alkempl.rlr.data.db.LocationEntity
import com.alkempl.rlr.data.model.scenario.GeofenceEntry
import com.alkempl.rlr.services.LocationManager
import java.util.*
import java.util.concurrent.ExecutorService

private const val TAG = "LocationRepository"

private const val OLD_THRESHOLD_DAYS = 3


/**
 * Access point for database (MyLocation data) and location APIs (start/stop location updates and
 * checking location update status).
 */
class LocationRepository private constructor(
    private val database: AppDatabase,
    private val myLocationManager: LocationManager,
    private val executor: ExecutorService
) {

    // Database related fields/methods:
    private val locationDao = database.locationDao()

    /**
     * Returns all recorded locations from database.
     */
    fun getLocations(): LiveData<List<LocationEntity>> = locationDao.getLocations()

    /**
     * Returns all recorded locations from database.
     */
    fun getLocationsByPeriod(dateFrom: Date, dateTo: Date): LiveData<List<LocationEntity>> =
        locationDao.getLocationsByPeriod(dateFrom, dateTo)

    // Not being used now but could in future versions.
    /**
     * Returns specific location in database.
     */
    fun getLocation(id: UUID): LiveData<LocationEntity> = locationDao.getLocation(id)

    // Not being used now but could in future versions.
    /**
     * Updates location in database.
     */
    fun updateLocation(myLocationEntity: LocationEntity) {
        executor.execute {
            locationDao.updateLocation(myLocationEntity)
        }
    }

    /**
     * Adds location to the database.
     */
    fun addLocation(myLocationEntity: LocationEntity) {
        executor.execute {
            locationDao.addLocation(myLocationEntity)
        }
    }

    /**
     * Clears old locations in the database.
     */
    fun wipeOldLocations() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -OLD_THRESHOLD_DAYS)

        executor.execute {
            locationDao.dropOldLocations(calendar.time)
        }
    }

    /**
     * Adds list of locations to the database.
     */
    fun addLocations(myLocationEntities: List<LocationEntity>) {
        executor.execute {
            locationDao.addLocations(myLocationEntities)
        }
    }

    fun setActiveGeofence(entry: GeofenceEntry){
        myLocationManager.setActiveGeofence(entry)
    }

    // Location related fields/methods:
    /**
     * Status of whether the app is actively subscribed to location changes.
     */
    val receivingLocationUpdates: LiveData<Boolean> = myLocationManager.receivingLocationUpdates

    /**
     * Subscribes to location updates.
     */
    @MainThread
    fun startLocationUpdates() = myLocationManager.startLocationUpdates()

    /**
     * Un-subscribes from location updates.
     */
    @MainThread
    fun stopLocationUpdates() = myLocationManager.stopLocationUpdates()

    companion object {
        @Volatile
        private var INSTANCE: LocationRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): LocationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationRepository(
                    AppDatabase.getInstance(context),
                    LocationManager.getInstance(context),
                    executor
                )
                    .also { INSTANCE = it }
            }
        }
    }
}