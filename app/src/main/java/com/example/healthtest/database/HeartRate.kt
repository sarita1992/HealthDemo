package com.example.healthtest.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HeartRate(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "heart_rate") var heartRate: Double?
)
