package com.alkempl.rlr.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

/**
 * Defines database operations.
 */
@Dao
interface LocationDao {

    @Query("SELECT * FROM location ORDER BY date DESC")
    fun getLocations(): LiveData<List<LocationEntity>>

    @Query("SELECT * FROM location WHERE id=(:id)")
    fun getLocation(id: UUID): LiveData<LocationEntity>

    @Update
    fun updateLocation(myLocationEntity: LocationEntity)

    @Insert
    fun addLocation(myLocationEntity: LocationEntity)

    @Insert
    fun addLocations(myLocationEntities: List<LocationEntity>)
}
