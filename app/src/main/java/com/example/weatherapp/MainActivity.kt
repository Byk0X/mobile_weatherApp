package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import coil3.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherApplication()
                }
            }

        }
    }
}


@Composable
fun WeatherApplication(viewModel: WeatherViewModel = viewModel()) {
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    val unitSystem by viewModel.unitSystem
    var forecast by remember { mutableStateOf<ForecastResponse?>(null) }
    val city by viewModel.currentCity


    LaunchedEffect(unitSystem, city) {
        viewModel.fetchWeather(
            city,
            units = unitSystem.apiValue,
            "d81c46127e231b83bd487579f8f556fe",
            "pl"
        ) { result ->
            weather = result
        }

        viewModel.fetchForecast(
            city,
            units = unitSystem.apiValue,
            "d81c46127e231b83bd487579f8f556fe",
            "pl"
        ) { result ->
            forecast = result
        }

    }
    WeatherScreenPager(weather, viewModel, forecast)

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreenPager(
    weatherResponse: WeatherResponse?,
    viewModel: WeatherViewModel,
    forecastResponse: ForecastResponse?
) {
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
                            overflow = TextOverflow.Clip
                        )
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
                    0 -> BasicWeatherFragment(weatherResponse, viewModel)
                    1 -> ExtraWeatherFragment(weatherResponse, viewModel)
                    2 -> ForecastFragment(forecastResponse, viewModel)
                    3 -> Favourites()
                    4 -> Settings(viewModel)
                }
            }

            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                NoInternetFooterChecker(viewModel)
            }
        }
    }
}


@Composable
fun BasicWeatherFragment(weatherResponse: WeatherResponse?, viewModel: WeatherViewModel) {

    val unitSystem by viewModel.unitSystem
    val iconCode = weatherResponse?.weather?.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        weatherResponse?.let { weather ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // main panel with localisation and temperature and weather description
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Lokalizacja",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = weather.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "${weather.main.temp.roundToInt()}${unitSystem.tempLabel}",
                                        style = MaterialTheme.typography.displayLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Odczuwalna: ${weather.main.feelsLike.roundToInt()}${unitSystem.tempLabel}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(
                                                    alpha = 0.3f
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = iconUrl,
                                            contentDescription = "Ikona pogody",
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = weather.weather.firstOrNull()?.description
                                            ?: "brak danych",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                //second panel with time coords and preasure
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {

                                val time = weather.dt
                                val formattedTime = remember(time) {
                                    val sdf = SimpleDateFormat(
                                        "HH:mm:ss, dd MMM yyyy",
                                        Locale("pl", "PL")
                                    )
                                    sdf.timeZone = java.util.TimeZone.getDefault()
                                    sdf.format(Date(time * 1000L))
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Czas: $formattedTime",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Współrzędne",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Szer.: ${weather.coord.lat}, Dł.: ${weather.coord.lon}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ciśnienie: ${weather.main.pressure} hPa",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }


            }
        }
    }
}

@Composable
fun ExtraWeatherFragment(weatherResponse: WeatherResponse?, viewModel: WeatherViewModel) {

    val unitSystem by viewModel.unitSystem

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        weatherResponse?.let { weather ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Wiatr: ${weather.wind.speed} ${unitSystem.speedLabel}, ${weather.wind.deg}°",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Wilgotność: ${weather.main.humidity}%",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Widoczność: ${weather.visibility / 1000.0} km",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun ForecastFragment(forecastResponse: ForecastResponse?, viewModel: WeatherViewModel) {

    val unitSystem by viewModel.unitSystem

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Prognoza pogody",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (forecastResponse == null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Ładowanie prognozy...",
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            Text(
                text = "${forecastResponse.city.name}, ${forecastResponse.city.country}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Forecast list
            LazyColumn {
                items(forecastResponse.list) { forecastItem ->
                    val dateFormatter = SimpleDateFormat("HH:mm, dd.MM", Locale("pl", "PL"))
                    val date = Date(forecastItem.dt * 1000)
                    val formattedDate = dateFormatter.format(date)
                    val iconCode = forecastItem.weather.firstOrNull()?.icon
                    "https://openweathermap.org/img/wn/${iconCode}@2x.png"

                    // Forecast card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //Left site
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = formattedDate,
                                    fontWeight = FontWeight.Medium
                                )

                                // Weather description
                                val description =
                                    forecastItem.weather.firstOrNull()?.description ?: ""
                                Text(
                                    text = description.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    },
                                    color = Color.Gray
                                )
                            }

                            // Right site
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                // Temperature
                                Text(
                                    text = "${forecastItem.main.temp.toInt()}${unitSystem.tempLabel}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Wind and humidity
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${forecastItem.main.humidity}%",
                                        fontSize = 14.sp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${forecastItem.wind.speed} ${unitSystem.speedLabel}",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun Favourites() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Ulubione")

    }
}

@Composable
fun Settings(viewModel: WeatherViewModel) {
    val unitSystem by viewModel.unitSystem
    val currentCity by viewModel.currentCity

    var cityInput by remember { mutableStateOf(currentCity) }

    val options = UnitSystem.entries

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Wybierz jednostkę:", style = MaterialTheme.typography.titleMedium)

        Column(Modifier.selectableGroup()) {
            options.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (option == unitSystem),
                            onClick = { viewModel.setUnitSystem(option) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (option == unitSystem),
                        onClick = { viewModel.setUnitSystem(option) }
                    )
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Aktualne miasto: $currentCity", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = cityInput,
            onValueChange = { cityInput = it },
            label = { Text("Wpisz nazwę miasta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.setCity(cityInput) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Zmień miasto")
        }
    }
}


@Composable
fun NoInternetFooterChecker(viewModel: WeatherViewModel) {
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