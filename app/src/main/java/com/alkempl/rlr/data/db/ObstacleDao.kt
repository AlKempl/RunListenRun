package com.alkempl.rlr.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

/**
 * Defines database operations.
 */
@Dao
interface ObstacleDao {

    @Query("SELECT * FROM obstacle ORDER BY date DESC")
    fun getAll(): LiveData<List<ObstacleEntity>>

    @Query("SELECT * FROM obstacle WHERE id=(:id)")
    fun getById(id: UUID): LiveData<ObstacleEntity>

    @Query("SELECT * FROM obstacle WHERE status='ongoing' ORDER BY date DESC")
    fun getOngoing(): LiveData<List<ObstacleEntity>>

    @Update
    fun update(myObstacleEntity: ObstacleEntity)

    @Insert
    fun add(myObstacleEntity: ObstacleEntity)

    @Insert
    fun add(myObstacleEntitiesList: List<ObstacleEntity>)

    @Query("DELETE FROM obstacle WHERE date <= (:date)")
    fun dropOld(date: Date)

    @Query("DELETE FROM obstacle")
    fun clearObstacles()
}
