package com.example.weatherapp

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.core.content.edit
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class WeatherViewModel : ViewModel() {

    fun initializeData(context: Context) {
        loadLastWeatherData(context)
        loadLastForecastData(context)
    }

    private val _weatherResponse = MutableStateFlow<WeatherResponse?>(null)
    val weatherResponse: StateFlow<WeatherResponse?> = _weatherResponse

    private val _forecastResponse = MutableStateFlow<ForecastResponse?>(null)
    val forecastResponse: StateFlow<ForecastResponse?> = _forecastResponse

    var isLoading by mutableStateOf(false)

    //default system is metric - after restart app sets metric
    private val _unitSystem = mutableStateOf(UnitSystem.Metric)
    val unitSystem: State<UnitSystem> = _unitSystem

    fun setUnitSystem(newUnit: UnitSystem) {
        _unitSystem.value = newUnit
    }

    //default localisation after installation
    private var _currentCity = mutableStateOf("Warszawa")
    val currentCity: State<String> = _currentCity

    fun setCity(newCity: String) {
        _currentCity.value = newCity
    }

    private val _lastWeatherData = mutableStateOf<WeatherResponse?>(null)
    val lastWeatherData: State<WeatherResponse?> = _lastWeatherData

    private val _lastForecastData = mutableStateOf<ForecastResponse?>(null)
    val lastForecastData: State<ForecastResponse?> = _lastForecastData

    fun saveRefreshInterval(context: Context, intervalMinutes: Int) {
        val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        prefs.edit { putInt("refresh_interval", intervalMinutes) }

        schedulePeriodicWeatherUpdates(context)
    }

    fun loadRefreshInterval(context: Context): Int {
        val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("refresh_interval", 60) // default 60 minutes
    }

    fun schedulePeriodicWeatherUpdates(context: Context) {
        val intervalMinutes = loadRefreshInterval(context)
        val city = currentCity.value
        val unitSystem = unitSystem.value.apiValue

        WeatherUpdateWorker.schedulePeriodicWork(
            context,
            intervalMinutes,
            city,
            unitSystem
        )
    }

    fun cancelPeriodicWeatherUpdates(context: Context) {
        WeatherUpdateWorker.cancelWork(context)
    }

    fun saveWeatherToPreferences(context: Context, data: WeatherResponse) {
        try {
            val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
            prefs.edit {

                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter(WeatherResponse::class.java)
                val json = adapter.toJson(data)

                putString("last_weather", json)
            }

            Log.d("WeatherVM", "Weather saved to SharedPreferences.")
        } catch (e: Exception) {
            Log.e("WeatherVM", "Error saving weather to preferences", e)
        }
    }

    fun loadWeatherFromPreferences(context: Context): WeatherResponse? {
        return try {
            val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("last_weather", null)
            Log.d("prefs", "Raw weather: $json")
            if (json.isNullOrEmpty()) return null

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(WeatherResponse::class.java)
            adapter.fromJson(json)

        } catch (e: Exception) {
            Log.e("WeatherVM", "Error loading weather from preferences", e)
            null
        }
    }

    fun saveForecastToPreferences(context: Context, data: ForecastResponse) {
        try {
            val prefs = context.getSharedPreferences("forecast_prefs", Context.MODE_PRIVATE)
            prefs.edit {

                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter(ForecastResponse::class.java)
                val json = adapter.toJson(data)

                putString("last_forecast", json)
            }

            Log.d("WeatherVM", "forecast saved to SharedPreferences.")
        } catch (e: Exception) {
            Log.e("WeatherVM", "Error saving forecast to preferences", e)
        }
    }

    fun loadForecastFromPreferences(context: Context): ForecastResponse? {
        return try {
            val prefs = context.getSharedPreferences("forecast_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("last_forecast", null)
            Log.d("prefs", "Raw forecast: $json")
            if (json.isNullOrEmpty()) return null

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(ForecastResponse::class.java)
            adapter.fromJson(json)

        } catch (e: Exception) {
            Log.e("WeatherVM", "Error loading forecast from preferences", e)
            null
        }
    }

    fun saveLastWeatherData(context: Context, data: WeatherResponse) {
        _lastWeatherData.value = data
        saveWeatherToPreferences(context, data)
    }

    fun loadLastWeatherData(context: Context) {
        val weather = loadWeatherFromPreferences(context)
        if (weather != null) {
            _lastWeatherData.value = weather
            _weatherResponse.value = weather
            _currentCity.value =  weather.name
        }
    }

    fun loadLastForecastData(context: Context) {
        val weather = loadForecastFromPreferences(context)
        if (weather != null) {
            _lastForecastData.value = weather
            _forecastResponse.value = weather
        }
    }

    fun saveLastForecastData(context: Context, data: ForecastResponse) {
        _lastForecastData.value = data
        saveForecastToPreferences(context, data)
    }

    fun fetchWeather(city: String, units: String, lang: String, onResult: (WeatherResponse?) -> Unit) {
        isLoading = true

        viewModelScope.launch {
            try {
                Log.d("WeatherVM", BuildConfig.WEATHER_API_KEY)
                val response = RetrofitClient.apiService.getWeather(cityNameWithoutSpecialLetters(city), units, BuildConfig.WEATHER_API_KEY ,lang)
                _weatherResponse.value = response
                onResult(response)
            } catch (e: Exception) {
                Log.e("WeatherVM", "Błąd przy pobieraniu pogody: ${e.message}")
                onResult(null)
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchForecast(city: String, units: String, lang: String, onResult: (ForecastResponse?) -> Unit){
        isLoading = true

        viewModelScope.launch{
            try {
                Log.d("WeatherVM", BuildConfig.WEATHER_API_KEY)
                val response = RetrofitClient.forecastApiService.getForecast(cityNameWithoutSpecialLetters(city), units, BuildConfig.WEATHER_API_KEY, lang)
                _forecastResponse.value = response
                Log.d("WeatherVM", BuildConfig.WEATHER_API_KEY)
                onResult(response)
            } catch (e: Exception) {
                Log.e("WeatherVM", "Błąd przy pobieraniu prognozy: ${e.message}")
                onResult(null)
            } finally {
                isLoading = false
            }
        }
    }


    //--------------favourite locations section------------------

    private val _favoriteLocations = mutableStateOf<List<String>>(emptyList())
    val favoriteLocations: State<List<String>> = _favoriteLocations

    fun loadFavoriteLocations(context: Context) {
        val prefs = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("favorites", null)
        if (json.isNullOrEmpty()) {
            _favoriteLocations.value = emptyList()
        } else {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
            _favoriteLocations.value = adapter.fromJson(json) ?: emptyList()
        }
    }

    fun saveFavoriteLocations(context: Context) {
        val prefs = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
        val json = adapter.toJson(_favoriteLocations.value)
        prefs.edit().putString("favorites", json).apply()
    }

    fun addFavoriteLocation(context: Context, city: String) {
        if (!_favoriteLocations.value.contains(city)) {
            _favoriteLocations.value = _favoriteLocations.value + city
            saveFavoriteLocations(context)
        }
    }

//    fun saveFavouritesForecastsToFiles(context: Context, forecasts: Map<String, ForecastResponse>) {
//        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
//        val adapter = moshi.adapter(ForecastResponse::class.java)
//
//        for ((city, forecast) in forecasts) {
//            try {
//                val json = adapter.toJson(forecast)
//                val fileName = "forecast_${city.lowercase().replace(" ", "_")}.json"
//                context.openFileOutput(fileName, Context.MODE_PRIVATE).use { it.write(json.toByteArray()) }
//                Log.d("WeatherVM", "Saved forecast for $city to $fileName")
//            } catch (e: Exception) {
//                Log.e("WeatherVM", "Error saving forecast for $city", e)
//            }
//        }
//    }

    fun loadForecastFromFile(context: Context, city: String) {
        val fileName = "forecast_${city.lowercase().replace(" ", "_")}.json"
        val file = File(context.filesDir, fileName)

        if (!file.exists()) {
            Log.w("WeatherVM", "Brak pliku z prognoza dla miasta: $city")
            return
        }

        try {
            val json = file.readText()
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(ForecastResponse::class.java)
            val weather = adapter.fromJson(json)

            if (weather != null) {
                _forecastResponse.value = weather
                _lastForecastData.value = weather
            } else {
                Log.w("WeatherVM", "Nie udało się sparsować danych pogodowych dla $city")
            }

        } catch (e: Exception) {
            Log.e("WeatherVM", "Błąd przy wczytywaniu prognozy z pliku dla $city", e)
        }
    }


    fun saveWeatherToFile(context: Context, city: String, weather: WeatherResponse?) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val file = File(context.filesDir, "weather_${city.lowercase()}.json")
        val json = moshi.adapter(WeatherResponse::class.java).toJson(weather)
        file.writeText(json)
    }

    fun saveForecastToFile(context: Context, city: String, forecast: ForecastResponse?) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val file = File(context.filesDir, "forecast_${city.lowercase()}.json")
        val json = moshi.adapter(ForecastResponse::class.java).toJson(forecast)
        file.writeText(json)
    }

    fun loadWeatherFromFile(context: Context, city: String) {
        val fileName = "weather_${city.lowercase().replace(" ", "_")}.json"
        val file = File(context.filesDir, fileName)

        if (!file.exists()) {
            Log.w("WeatherVM", "Brak pliku z pogodą dla miasta: $city")
            return
        }

        try {
            val json = file.readText()
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(WeatherResponse::class.java)
            val weather = adapter.fromJson(json)

            if (weather != null) {
                _weatherResponse.value = weather
                _lastWeatherData.value = weather
                _currentCity.value = weather.name
            } else {
                Log.w("WeatherVM", "Nie udało się sparsować danych pogodowych dla $city")
            }

        } catch (e: Exception) {
            Log.e("WeatherVM", "Błąd przy wczytywaniu pogody z pliku dla $city", e)
        }
    }

    fun removeFavoriteLocation(context: Context, city: String) {
        if (_favoriteLocations.value.contains(city)) {
            _favoriteLocations.value = _favoriteLocations.value - city
            saveFavoriteLocations(context)
        }
    }



    //-----------------------------------------------------------


}
