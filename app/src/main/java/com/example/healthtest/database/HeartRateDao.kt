package com.example.healthtest.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HeartRateDao {
    @Query("SELECT * FROM heartrate")
    fun getAll(): List<HeartRate>

    @Query("SELECT * FROM heartrate WHERE uid IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<HeartRate>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<HeartRate>)

    @Delete
    fun delete(user: HeartRate)
}