package com.example.myapplication6.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WeatherData")
data class WeatherData(
    @PrimaryKey
    val id: String,
    val date: String,
    val maxTemp: Float,
    val minTemp: Float,
    val latitude: Double,
    val longitude: Double
)
