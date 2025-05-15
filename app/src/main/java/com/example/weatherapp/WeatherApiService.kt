package com.example.weatherapp

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("units") units: String,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String
    ): WeatherResponse
}

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val coord: Coordinates,
    val weather: List<WeatherCondition>,
    val base: String,
    val main: MainData,
    val visibility: Int,
    val wind: WindData,
    val rain: Rain? = null,
    val clouds: CloudsData,
    val dt: Long,
    val sys: SysData,
    val timezone: Int,
    val id: Long,
    val name: String,
    val cod: Int
)

@JsonClass(generateAdapter = true)
data class MainData(
    val temp: Double,
    @Json(name = "feels_like") val feelsLike: Double,
    @Json(name = "temp_min") val tempMin: Double,
    @Json(name = "temp_max") val tempMax: Double,
    val pressure: Int,
    val humidity: Int
)

@JsonClass(generateAdapter = true)
data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class WindData(
    val speed: Double,
    val deg: Int,
    val gust: Double?
)

@JsonClass(generateAdapter = true)
data class CloudsData(
    val all: Int
)

@JsonClass(generateAdapter = true)
data class SysData(
    val type: Int?,
    val id: Int?,
    val country: String,
    val sunrise: Long,      // Czas wschodu słońca (unix timestamp)
    val sunset: Long        // Czas zachodu słońca (unix timestamp)
)

@JsonClass(generateAdapter = true)
data class Coordinates(
    val lon: Double,
    val lat: Double
)

@JsonClass(generateAdapter = true)
data class Rain(
    @Json(name = "1h") val lastHour: Double? = null
)
