/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.healthtest.fetchdata

import androidx.annotation.WorkerThread
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.HeartRateAccuracy
import androidx.health.services.client.data.HeartRateAccuracy.SensorStatus.Companion.ACCURACY_HIGH
import androidx.health.services.client.data.HeartRateAccuracy.SensorStatus.Companion.ACCURACY_MEDIUM
import androidx.health.services.client.data.SampleDataPoint
import com.example.healthtest.database.HeartRate
import com.example.healthtest.database.HeartRateDao
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


/**
 * Stores heart rate measurements and whether or not passive data is enabled.
 */
class PassiveDataRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val passiveDataEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PASSIVE_DATA_ENABLED] ?: false
    }

    suspend fun setPassiveDataEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PASSIVE_DATA_ENABLED] = enabled
        }
    }

    val latestHeartRate: Flow<Double> = dataStore.data.map { prefs ->
        prefs[LATEST_HEART_RATE] ?: 0.0
    }
    val latestHeartRateJson: Flow<String> = dataStore.data.map { prefs ->
        prefs[HEART_RATE] ?: ""
    }


    suspend fun storeLatestHeartRate(heartRate: Double) {
        val heart = HeartRate(list.size, heartRate)
        list.add(heart)
        val gson = Gson()

        // getting data from gson and storing it in a string.

        // getting data from gson and storing it in a string.
        val json = gson.toJson(list)

        dataStore.edit { prefs ->
            prefs[HEART_RATE] = json
        }
        dataStore.edit { prefs ->
            prefs[LATEST_HEART_RATE] = heartRate
        }
    }


    companion object {
        const val PREFERENCES_FILENAME = "health_test_prefs"
        private val PASSIVE_DATA_ENABLED = booleanPreferencesKey("health_test_enabled")
        private val LATEST_HEART_RATE = doublePreferencesKey("latest_heart_rate")
        private val HEART_RATE = stringPreferencesKey("heart_rate")
        var list = ArrayList<HeartRate>()
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(word: List<HeartRate>, dao: HeartRateDao) {
        dao.insertAll(word)
        list.clear()
    }
}

fun List<SampleDataPoint<Double>>.latestHeartRate(): Double? {
    return this
        // dataPoints can have multiple types (e.g. if the app is registered for multiple types).
        .filter { it.dataType == DataType.HEART_RATE_BPM }
        // where accuracy information is available, only show readings that are of medium or
        // high accuracy. (Where accuracy information isn't available, show the reading if it is
        // a positive value).
        .filter {
            it.accuracy == null || setOf(
                ACCURACY_HIGH, ACCURACY_MEDIUM
            ).contains((it.accuracy as HeartRateAccuracy).sensorStatus)
        }.filter {
            it.value > 0
        }
        // HEART_RATE_BPM is a SAMPLE type, so start and end times are the same.
        .maxByOrNull { it.timeDurationFromBoot }?.value
}