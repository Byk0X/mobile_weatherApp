package com.example.weatherapp

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class WeatherUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val viewModel = WeatherViewModel()
        val city = inputData.getString("city") ?: viewModel.currentCity.value
        val unitSystem = inputData.getString("unit_system") ?: viewModel.unitSystem.value.apiValue

        viewModel.fetchWeather(city, unitSystem, "pl") {}
        viewModel.fetchForecast(city, unitSystem, "pl") {}

        logRefreshToFile(applicationContext)

        return Result.success()
    }

    private fun logRefreshToFile(context: Context) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "Odświeżono: $timestamp\n"

        try {
            val file = File(context.filesDir, "weather_log.txt")
            file.appendText(logEntry)
        } catch (e: Exception) {
            Log.e("WeatherUpdateWorker", "Błąd przy zapisie logu: ${e.message}")
        }
    }

    companion object {
        private const val WORK_NAME = "weather_update_work"

        fun schedulePeriodicWork(
            context: Context,
            intervalMinutes: Int,
            city: String,
            unitSystem: String
        ) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)

            val actualIntervalMinutes = maxOf(15, intervalMinutes)

            val inputData = workDataOf(
                "city" to city,
                "unit_system" to unitSystem
            )

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val updateRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                actualIntervalMinutes.toLong(), TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                updateRequest
            )
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
