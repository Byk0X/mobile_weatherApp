package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.theme.WeatherAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    WeatherScreenPager(weather)

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreenPager(weatherResponse: WeatherResponse?) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    val tabTitles = listOf("Podstawowe", "Dodatkowe", "Prognoza", "Ulubione", "Ustawienia")

    Column {
        CenterAlignedTopAppBar(
            title = {
                Text(text = tabTitles[pagerState.currentPage])
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            maxLines = 2,
                            overflow = TextOverflow.Clip)
                    }
                )
            }
        }


        Box(modifier = Modifier.weight(1f)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> BasicWeatherFragment(weatherResponse)
                    1 -> ExtraWeatherFragment(weatherResponse)
                    2 -> ForecastFragment(weatherResponse)
                    3 -> Favourites()
                    4 -> Settings()
                }
            }

            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                NoInternetFooterChecker()
            }
        }
    }
}


@Composable
fun BasicWeatherFragment(weatherResponse: WeatherResponse?) {
    Column(modifier = Modifier.padding(16.dp)) {
        weatherResponse?.let {
            Column {
                Text("Miasto: ${it.name}")
                Text("Temperatura: ${it.main.temp} °C")
                Text("Opis: ${it.weather.firstOrNull()?.description ?: "brak"}")
                Text("Opady: ${it.rain?.lastHour ?: 0.0} mm")
            }
        }

    }
}

@Composable
fun ExtraWeatherFragment(weatherResponse: WeatherResponse?) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Dodatkowe")

    }
}

@Composable
fun ForecastFragment(weatherResponse: WeatherResponse?) {
    Column(modifier = Modifier.padding(16.dp)) {

            Text("Progrnoza")

    }
}

@Composable
fun Favourites() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Ulubione")

    }
}

@Composable
fun Settings() {

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Ustawienia")

    }
}

@Composable
fun NoInternetFooterChecker() {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            isConnected = NetworkChecker(context).isConnected()
            delay(3000) // co 3 sekundy
        }
    }

    if (!isConnected) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Brak połączenia z internetem",
                color = Color.White
            )
        }
    }
}