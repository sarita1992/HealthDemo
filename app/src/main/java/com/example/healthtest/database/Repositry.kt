package com.example.healthtest.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class Repositry (private val wordDao: HeartRateDao) {


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(word: List<HeartRate>) {
        wordDao.insertAll(word)
    }
}