package com.example.weatherapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.State

class WeatherViewModel : ViewModel() {

    var weatherResponse by mutableStateOf<WeatherResponse?>(null)
        private set

    var forecastResponse by mutableStateOf<ForecastResponse?>(null)
        private set

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

    fun fetchWeather(city: String, units: String, apiKey: String, lang: String, onResult: (WeatherResponse?) -> Unit) {
        isLoading = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getWeather(city, units ,apiKey, lang)
                weatherResponse = response
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
                forecastResponse = response
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