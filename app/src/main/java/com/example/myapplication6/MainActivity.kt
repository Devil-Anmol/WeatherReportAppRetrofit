package com.example.myapplication6

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication6.DataClass.WeatherResponse
import com.example.myapplication6.Database.WeatherDao
import com.example.myapplication6.Database.WeatherData
import com.example.myapplication6.Database.WeatherDatabase
import com.example.myapplication6.ui.theme.MyApplication6Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplication6Theme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherApp()
                }
            }
        }
    }
}

//
//@Entity(tableName = "WeatherData")
//data class WeatherData(
//    @PrimaryKey
//    val id: String,
//    val date: String,
//    val maxTemp: Float,
//    val minTemp: Float,
//    val latitude: Double,
//    val longitude: Double
//)


//data class Average(
//    val maxTemp: Float,
//    val minTemp: Float
//)


@Composable
fun WeatherApp() {
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://archive-api.open-meteo.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val context = LocalContext.current
    val db = Room.databaseBuilder(
        context,
        WeatherDatabase::class.java, "database-name"
    ).build()

    var date by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val weatherDao = db.weatherDao()

    val weatherData = remember {
        mutableStateOf<WeatherData?>(null)
    }

    val averageWeatherData = remember {
        mutableStateOf<Average?>(null)
    }
    var latitude by remember { mutableStateOf(0.0) }
    var l1 by remember { mutableStateOf("") }
    var l2 by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf(0.0) }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = com.example.myapplication6.R.drawable.sky),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 1f
        )
    }

    Column {
        Text(
            text = "Weather App",
            style = MaterialTheme.typography.displayLarge,
            color = Color.Black,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        TextField(
            value = l1,
            onValueChange = { newValue ->
                l1 = newValue.trim()
                latitude = l1.toDoubleOrNull() ?: 0.0
            },
            textStyle = MaterialTheme.typography.titleLarge,
            placeholder = { Text("Enter Latitude") }
        )

        TextField(
            value = l2,
            onValueChange = { newValue ->
                l2 = newValue.trim()
                longitude = l2.toDoubleOrNull() ?: 0.0
            },
            textStyle = MaterialTheme.typography.titleLarge,
            placeholder = { Text("Enter Longitude") }
        )

        TextField(
            value = date,
            onValueChange = { date = it.trim() },
            label = { Text("Enter date (YYYY-MM-DD)") }
        )
        Button(onClick = {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.isLenient = false
            try {
                val inputDate = dateFormat.parse(date)
                val currentDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -2) // Subtract 2 days from the current date
                }.time
                errorMessage = null

                if (inputDate.after(currentDate)) {
                    weatherData.value=null
                    (currentDate.toString())
                    val tenyearsback = getDateTenYearsBack().toString();
                    val averageLiveData = weatherDao.getAverageOfLastTenYears(date,tenyearsback, latitude ,longitude)
                    averageLiveData.observeForever { averageData ->
                        averageWeatherData.value = averageData
                    }
                } else {
                    averageWeatherData.value=null
                    var ids = latitude.toString()+longitude.toString()+date
                    val localDataLiveData = weatherDao.getWeatherData(ids)
                    localDataLiveData.observeForever { localData ->
                        if (localData != null) {
                            weatherData.value = localData
                        } else {
                            if (isNetworkAvailable(context)) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val weatherService = retrofit.create(WeatherApiService::class.java)

                                    val latitude = 22.0
                                    val longitude = 79.0
                                    val data =
                                        fetchWeather(latitude, longitude, date, date, weatherService)
                                    if (data != null) {
                                        weatherDao.insertdata(data)
                                        weatherData.value = data
                                    } else {
                                        errorMessage = "API request failed."
                                    }
                                }
                            } else {
                                errorMessage = "Data not in database and their is no network connection available."
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Please enter date in the format (YYYY-MM-DD)."
                weatherData.value = null
            }
        }) {
            Text("Get Weather Data")
        }
        errorMessage?.let {
            Toast.makeText(LocalContext.current, "Error: $it", Toast.LENGTH_SHORT).show()
            errorMessage = null
        }
        weatherData.value?.let { data ->
            Text("Max Temperature: ${data.maxTemp}")
            Text("Min Temperature: ${data.minTemp}")
        }
        averageWeatherData.value?.let { data ->
            Text("Average Max Temp : ${data.maxTemp}")
            Text("Average Min Temp : ${data.minTemp}")
        }
    }
}

fun getDateTenYearsBack(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.now().minusYears(10).format(formatter)
}

//
//@Composable
//fun WeatherApp() {
//    val retrofit = remember {
//        Retrofit.Builder()
//            .baseUrl("https://archive-api.open-meteo.com/v1/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//    val context = LocalContext.current
//    val db = Room.databaseBuilder(
//        context,
//        AppDatabase::class.java, "database-name"
//    ).build()
//
//    var date by remember { mutableStateOf("") }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    val weatherDao = db.weatherDao()
//
//    val weatherData = remember {
//        mutableStateOf<WeatherData?>(null)
//    }
//
//    val averageWeatherData = remember {
//        mutableStateOf<AverageWeatherData?>(null)
//    }
//
//    Column {
//        TextField(
//            value = date,
//            onValueChange = { date = it },
//            label = { Text("Enter date (YYYY-MM-DD)") }
//        )
//        Button(onClick = {
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
//            dateFormat.isLenient = false
//            try {
//                val inputDate = dateFormat.parse(date)
//                val currentDate = Date()
//                errorMessage = null
//
//                if (inputDate.after(currentDate)) {
//                    weatherData.value=null
//                    val averageLiveData = weatherDao.getAverageOfLastTenYears(date)
//                    averageLiveData.observeForever { averageData ->
//                        averageWeatherData.value = averageData
//                    }
//                } else {
//                    averageWeatherData.value=null
//                    val localDataLiveData = weatherDao.getWeatherData(date)
//                    localDataLiveData.observeForever { localData ->
//                        if (localData != null) {
//                            weatherData.value = localData
//                        } else {
//                            if (isNetworkAvailable(context)) {
//                                CoroutineScope(Dispatchers.IO).launch {
//
//                                    val weatherService = retrofit.create(WeatherApiService::class.java)
//
//                                    val latitude = 22.0
//                                    val longitude = 79.0
//                                    val data =
//                                        fetchWeather(latitude, longitude, date, date, weatherService)
//                                    if (data != null) {
//                                        weatherDao.insert(data)
//                                        weatherData.value = data
//                                    } else {
//                                        errorMessage = "Network request failed."
//                                    }
//                                }
//                            } else {
//                                errorMessage = "No network connection available."
//                            }
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                errorMessage = "Invalid date format. Please enter a date in the format YYYY-MM-DD."
//                weatherData.value = null
//            }
//        }) {
//            Text("Fetch Weather Data")
//        }
//        errorMessage?.let {
//            // Display the error message as a toast
//            Toast.makeText(LocalContext.current, "Error: $it", Toast.LENGTH_SHORT).show()
//        }
//        weatherData.value?.let { data ->
//            Text("Max Temperature: ${data.maxTemp}")
//            Text("Min Temperature: ${data.minTemp}")
//        }
//        averageWeatherData.value?.let { data ->
//            Text("Average Max Temperature for last 10 years: ${data.maxTemp}")
//            Text("Average Min Temperature for last 10 years: ${data.minTemp}")
//        }
//    }
//}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager?.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}


//data class Weather(val date : String,val maxTemperature: Float, val minTemperature: Float)

//data class WeatherResponse(
//    val latitude: Double,
//    val longitude: Double,
//    val generationtime_ms: Double,
//    val utc_offset_seconds: Int,
//    val timezone: String,
//    val timezone_abbreviation: String,
//    val elevation: Int,
//    val daily_units: DailyUnits,
//    val daily: Daily
//)

//data class DailyUnits(
//    val time: String,
//    val temperature_2m_max: String,
//    val temperature_2m_min: String
//)

//data class Daily(
//    val time: List<String>,
//    val temperature_2m_max: List<Float>,
//    val temperature_2m_min: List<Float>
//)

interface WeatherApiService {
    @GET("archive")
    suspend fun getWeatherData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("daily") daily: String
    ): WeatherResponse
}

//suspend fun fetchWeather(
//    latitude: Double,
//    longitude: Double,
//    startDate: String,
//    endDate: String,
//    weatherService: WeatherApiService
//): WeatherData? {
//    return withContext(Dispatchers.IO) {
//        try {
//            val response = weatherService.getWeatherData(latitude, longitude, startDate, endDate, "temperature_2m_max,temperature_2m_min")
//            val date = response.daily.time.firstOrNull()
//            val maxTemperature = response.daily.temperature_2m_max.firstOrNull()
//            val minTemperature = response.daily.temperature_2m_min.firstOrNull()
//            // Check if any data is missing
//            if (date != null && maxTemperature != null && minTemperature != null) {
//                val id = latitude.toString()+longitude.toString()+date
//                WeatherData(id,date, maxTemperature, minTemperature,latitude,longitude)
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            null
//        }
//    }
//}
