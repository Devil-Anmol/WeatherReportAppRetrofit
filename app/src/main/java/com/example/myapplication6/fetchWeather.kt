package com.example.myapplication6

import com.example.myapplication6.Database.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun fetchWeather(
    latitude: Double,
    longitude: Double,
    startDate: String,
    endDate: String,
    weatherService: WeatherApiService
): WeatherData? {
    return withContext(Dispatchers.IO) {
        try {
            val response = weatherService.getWeatherData(latitude, longitude, startDate, endDate, "temperature_2m_max,temperature_2m_min")
            val date = response.daily.time.firstOrNull()
            val maxTemperature = response.daily.temperature_2m_max.firstOrNull()
            val minTemperature = response.daily.temperature_2m_min.firstOrNull()
            // Check if any data is missing
            if (date != null && maxTemperature != null && minTemperature != null) {
                val id = latitude.toString()+longitude.toString()+date
                WeatherData(id,date, maxTemperature, minTemperature,latitude,longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}