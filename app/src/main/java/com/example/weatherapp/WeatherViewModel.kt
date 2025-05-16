package com.example.weatherapp

import android.content.Context
import android.util.Log
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
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class WeatherViewModel : ViewModel() {


    private val _weatherResponse = MutableStateFlow<WeatherResponse?>(null)
    val weatherResponse: StateFlow<WeatherResponse?> = _weatherResponse

    private val _forecastResponse = MutableStateFlow<ForecastResponse?>(null)
    val forecastResponse: StateFlow<ForecastResponse?> = _forecastResponse

    fun saveLastWeatherData(data: WeatherResponse) {
        _weatherResponse.value = data
    }

    fun saveLastForecastData(data: ForecastResponse) {
        _forecastResponse.value = data
    }

    var isLoading by mutableStateOf(false)

    private val _unitSystem = mutableStateOf(UnitSystem.Metric)
    val unitSystem: State<UnitSystem> = _unitSystem

    fun setUnitSystem(newUnit: UnitSystem) {
        _unitSystem.value = newUnit
    }

    private var _currentCity = mutableStateOf("Lodz")
    val currentCity: State<String> = _currentCity

    fun setCity(newCity: String) {
        _currentCity.value = newCity
    }

    private val _lastWeatherData = mutableStateOf<WeatherResponse?>(null)
    val lastWeatherData: State<WeatherResponse?> = _lastWeatherData

    private val _lastForecastData = mutableStateOf<ForecastResponse?>(null)
    val lastForecastData: State<ForecastResponse?> = _lastForecastData


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

    fun fetchWeather(city: String, units: String, apiKey: String, lang: String, onResult: (WeatherResponse?) -> Unit) {
        isLoading = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getWeather(city, units ,apiKey, lang)
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

    fun fetchForecast(city: String, units: String, apiKey: String, lang: String, onResult: (ForecastResponse?) -> Unit){
        isLoading = true

        viewModelScope.launch{
            try {
                val response = RetrofitClient.forecastApiService.getForecast(city, units, apiKey, lang)
                _forecastResponse.value = response
                onResult(response)
            } catch (e: Exception) {
                Log.e("WeatherVM", "Błąd przy pobieraniu prognozy: ${e.message}")
                onResult(null)
            } finally {
                isLoading = false
            }
        }
    }
}
