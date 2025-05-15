package com.example.weatherapp

enum class UnitSystem(val apiValue: String, val label: String, val tempLabel: String, val speedLabel: String) {

    Metric("metric", "Metryczne (°C, km/h)", "°C", "km/h"),
    Imperial("imperial", "Imperialne (°F, mph)", "°F", "mph"),
    Standard("standard", "Standardowe (K, m/s)", "K", "m/s")
}