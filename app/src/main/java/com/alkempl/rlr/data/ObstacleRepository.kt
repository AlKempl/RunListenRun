package com.alkempl.rlr.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.alkempl.rlr.data.db.AppDatabase
import com.alkempl.rlr.data.db.ObstacleEntity
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
     * Returns all recorded obstacles from database.
     */
    fun getAll(): LiveData<List<ObstacleEntity>> = obstacleDao.getAll()

    /**
     * Returns all recorded obstacles from database.
     */
    fun getOngoing(): LiveData<List<ObstacleEntity>> = obstacleDao.getOngoing()

    // Not being used now but could in future versions.
    /**
     * Returns specific location in database.
     */
    fun getById(id: UUID): LiveData<ObstacleEntity> = obstacleDao.getById(id)

    // Not being used now but could in future versions.
    /**
     * Updates location in database.
     */
    fun update(myObstacleEntity: ObstacleEntity) {
        executor.execute {
            obstacleDao.update(myObstacleEntity)
        }
    }

    /**
     * Adds obstacles to the database.
     */
    fun add(myObstacleEntity: ObstacleEntity) {
        executor.execute {
            obstacleDao.add(myObstacleEntity)
        }
    }

    /**
     * Clears old obstacles in the database.
     */
    fun wipeOld() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -OLD_THRESHOLD_DAYS)

        executor.execute {
            obstacleDao.dropOld(calendar.time)
        }
    }

    /**
     * Clears all obstacles in the database.
     */
    fun clearAll() {
        executor.execute {
            obstacleDao.clearObstacles()
        }
    }

    /**
     * Adds list of obstacles to the database.
     */
    fun add(myObstacleEntities: List<ObstacleEntity>) {
        executor.execute {
            obstacleDao.add(myObstacleEntities)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ObstacleRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): ObstacleRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ObstacleRepository(
                    AppDatabase.getInstance(context),
                    executor
                )
                    .also { INSTANCE = it }
            }
        }
    }
}