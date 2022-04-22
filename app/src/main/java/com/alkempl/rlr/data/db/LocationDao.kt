package com.alkempl.rlr.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

/**
 * Defines database operations.
 */
@Dao
interface LocationDao {

    @Query("SELECT * FROM location ORDER BY date DESC")
    fun getLocations(): LiveData<List<LocationEntity>>

    @Query("SELECT * FROM location WHERE date between (:dateFrom) and (:dateTo) ORDER BY date DESC")
    fun getLocationsByPeriod(dateFrom: Date, dateTo: Date): LiveData<List<LocationEntity>>

    @Query("SELECT * FROM location WHERE id=(:id)")
    fun getLocation(id: UUID): LiveData<LocationEntity>

    @Update
    fun updateLocation(myLocationEntity: LocationEntity)

    @Insert
    fun addLocation(myLocationEntity: LocationEntity)

    @Insert
    fun addLocations(myLocationEntities: List<LocationEntity>)

    @Query("DELETE FROM location WHERE date <= (:date)")
    fun dropOldLocations(date: Date)
}
