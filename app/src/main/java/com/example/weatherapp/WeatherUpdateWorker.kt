package com.example.weatherapp

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "weather_update_work"

        fun schedulePeriodicWork(
            context: Context,
            intervalMinutes: Int,
            city: String,
            unitSystem: String
        ) {
            // Cancel all previous tasks
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)

            // Minimalna częstotliwość to 15 minut
            val actualIntervalMinutes = maxOf(15, intervalMinutes)


            val inputData = workDataOf(
                "city" to city,
                "unit_system" to unitSystem
            )

            // constraint: need network connection
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // create new task
            val updateRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                actualIntervalMinutes.toLong(), TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            // schedule task
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