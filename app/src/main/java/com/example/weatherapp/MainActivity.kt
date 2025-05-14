package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    WeatherApplication()
                }
            }

        }
    }
}


@Composable
fun WeatherApplication(viewModel: WeatherViewModel = viewModel()) {
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchWeather("Lodz", "d81c46127e231b83bd487579f8f556fe") { result ->
            weather = result
        }
    }

    weather?.let {
        Column {
            Text("Miasto: ${it.name}")
            Text("Temperatura: ${it.main.temp} °C")
            Text("Opis: ${it.weather.firstOrNull()?.description ?: "brak"}")
            Text("Opady: ${it.rain?.lastHour ?: 0.0} mm")
        }
    }
}
