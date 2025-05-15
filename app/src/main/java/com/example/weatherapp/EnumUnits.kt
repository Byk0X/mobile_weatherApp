package com.example.weatherapp

enum class UnitSystem(val apiValue: String, val label: String) {
    Standard("standard", "Standard (K, m/s)"),
    Metric("metric", "Metrical (°C, km/h)"),
    Imperial("imperial", "Imperial (°F, mph)")
}

