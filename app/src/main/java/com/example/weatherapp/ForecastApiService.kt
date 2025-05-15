package com.example.weatherapp


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query


interface ForecastApiService {
    @GET("forecast")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("units") units: String,
        @Query("appid") apiKey: String,
        @Query("lang") lang: String,
    ): ForecastResponse
}


@JsonClass(generateAdapter = true)
data class ForecastResponse(
    val cod: String,
    val message: Double,
    val cnt: Int,
    val list: List<ForecastEntry>,
    val city: ForecastCity
)

@JsonClass(generateAdapter = true)
data class ForecastEntry(
    val dt: Long,
    val main: MainDataForecast,
    val weather: List<WeatherConditionForecast>,
    val clouds: CloudsData,
    val wind: WindData,
    val visibility: Int,
    val pop: Double,
    val rain: RainForecast? = null,
    val sys: ForecastSys,
    @Json(name = "dt_txt") val dtTxt: String
)

@JsonClass(generateAdapter = true)
data class MainDataForecast(
    val temp: Double,
    @Json(name = "feels_like") val feelsLike: Double,
    @Json(name = "temp_min") val tempMin: Double,
    @Json(name = "temp_max") val tempMax: Double,
    val pressure: Int,
    @Json(name = "sea_level") val seaLevel: Int,
    @Json(name = "grnd_level") val grndLevel: Int,
    val humidity: Int,
    @Json(name = "temp_kf") val tempKf: Double
)


@JsonClass(generateAdapter = true)
data class RainForecast(
    @Json(name = "3h") val threeHour: Double?
)

@JsonClass(generateAdapter = true)
data class WeatherConditionForecast(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class ForecastSys(
    val pod: String // "d" or "n"
)

@JsonClass(generateAdapter = true)
data class ForecastCity(
    val id: Long,
    val name: String,
    val coord: Coordinates,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long
)

