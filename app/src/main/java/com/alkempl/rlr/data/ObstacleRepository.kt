package com.alkempl.rlr.data

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.alkempl.rlr.data.db.AppDatabase
import com.alkempl.rlr.data.db.LocationEntity
import com.alkempl.rlr.data.db.ObstacleEntity
import com.alkempl.rlr.services.LocationManager
import java.util.*
import java.util.concurrent.ExecutorService

private const val TAG = "ObstacleRepository"

private const val OLD_THRESHOLD_DAYS = 3;


/**
 * Access point for database (MyLocation data) and location APIs (start/stop location updates and
 * checking location update status).
 */
class ObstacleRepository private constructor(
    private val database: AppDatabase,
    private val executor: ExecutorService
) {

    // Database related fields/methods:
    private val obstacleDao = database.obstacleDao()

    /**
     * Returns all recorded locations from database.
     */
    fun getObstacles(): LiveData<List<ObstacleEntity>> = obstacleDao.getObstacles()

    // Not being used now but could in future versions.
    /**
     * Returns specific location in database.
     */
    fun getObstacle(id: UUID): LiveData<ObstacleEntity> = obstacleDao.getObstacle(id)

    // Not being used now but could in future versions.
    /**
     * Updates location in database.
     */
    fun updateObstacle(myObstacleEntity: ObstacleEntity) {
        executor.execute {
            obstacleDao.updateObstacle(myObstacleEntity)
        }
    }

    /**
     * Adds location to the database.
     */
    fun addObstacle(myObstacleEntity: ObstacleEntity) {
        executor.execute {
            obstacleDao.addObstacle(myObstacleEntity)
        }
    }

    /**
     * Clears old locations in the database.
     */
    fun wipeOldObstacles() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -OLD_THRESHOLD_DAYS)

        executor.execute {
            obstacleDao.dropOldObstacles(calendar.time)
        }
    }

    /**
     * Adds list of locations to the database.
     */
    fun addObstacles(myObstacleEntities: List<ObstacleEntity>) {
        executor.execute {
            obstacleDao.addObstacles(myObstacleEntities)
        }
    }

    companion object {
        @Volatile private var INSTANCE: ObstacleRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): ObstacleRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ObstacleRepository(
                    AppDatabase.getInstance(context),
                    executor)
                    .also { INSTANCE = it }
            }
        }
    }
}