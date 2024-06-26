package com.example.footixappbachelorarbeit.viewModelLiveData

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "session")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "current_date") var currentDate: String,
    @ColumnInfo(name = "total_distance") var totalDistance: Double,
    @ColumnInfo(name = "max_speed") var maxSpeed: Float,
    @ColumnInfo(name = "run_time") var runTime: String
)