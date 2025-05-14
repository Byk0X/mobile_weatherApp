package com.example.weatherapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    var weatherResponse by mutableStateOf<WeatherResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)

    // Metoda fetchWeather, która wykonuje zapytanie do API
    fun fetchWeather(city: String, apiKey: String, onResult: (WeatherResponse?) -> Unit) {
        // Ustawiamy loading na true podczas pobierania danych
        isLoading = true

        // Uruchamiamy korutynę w viewModelScope
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getWeather(city, apiKey)
                weatherResponse = response
                onResult(response)  // Zwracamy dane do Composables
            } catch (e: Exception) {
                Log.e("WeatherVM", "Błąd przy pobieraniu pogody: ${e.message}")
                onResult(null)
            } finally {
                isLoading = false  // Ustawiamy loading na false, po zakończeniu
            }
        }
    }
}