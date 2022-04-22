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
    fun getObstacles(): LiveData<List<ObstacleEntity>>

    @Query("SELECT * FROM obstacle WHERE id=(:id)")
    fun getObstacle(id: UUID): LiveData<ObstacleEntity>

    @Update
    fun updateObstacle(myObstacleEntity: ObstacleEntity)

    @Insert
    fun addObstacle(myObstacleEntity: ObstacleEntity)

    @Insert
    fun addObstacles(myObstacleEntity: List<ObstacleEntity>)

    @Query("DELETE FROM obstacle WHERE date <= (:date)")
    fun dropOldObstacles(date: Date)
}
