package com.example.myapplication6.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication6.Average


@Dao
interface WeatherDao {
    @Query("SELECT * FROM weatherdata WHERE id = :id")
    fun getWeatherData(id: String): LiveData<WeatherData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertdata(weatherData: WeatherData)

    @Query("SELECT AVG(maxTemp) as maxTemp, AVG(minTemp) as minTemp FROM weatherdata WHERE date < :date and date > :current and latitude= :latitude and longitude =:longitude ORDER BY date")
    fun getAverageOfLastTenYears(date: String,current: String, latitude: Double,longitude: Double): LiveData<Average?>
}