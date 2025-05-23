package com.example.weatherapp

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun WeatherApplication(viewModel: WeatherViewModel = viewModel()) {
    val context = LocalContext.current

    viewModel.schedulePeriodicWeatherUpdates(context)
    viewModel.loadFavoriteLocations(context)

    val weather by viewModel.weatherResponse.collectAsState()
    val forecast by viewModel.forecastResponse.collectAsState()
    val unitSystem by viewModel.unitSystem
    val city by viewModel.currentCity

    LaunchedEffect(Unit) {
        viewModel.initializeData(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancelPeriodicWeatherUpdates(context)
        }
    }


    LaunchedEffect(unitSystem, city) {
//        val isConnected = NetworkChecker(context).isConnected()

        if (city !in viewModel.favoriteLocations.value) {
            viewModel.fetchWeather(city, units = unitSystem.apiValue, lang = "pl") { result ->

                //Toast.makeText(context,"JEST ŁĄCZE", Toast.LENGTH_SHORT).show()
                if (result != null) {
                    viewModel.saveLastWeatherData(context, result)
                } else {
                    viewModel.loadLastWeatherData(context)
                }
            }

            viewModel.fetchForecast(city, units = unitSystem.apiValue, lang = "pl") { result ->
                if (result != null) {
                    viewModel.saveLastForecastData(context, result)
                } else {
                    viewModel.loadLastForecastData(context)
                }
            }
        } else if (city in viewModel.favoriteLocations.value) {
            //Toast.makeText(context,"NIE MA ŁĄCZa", Toast.LENGTH_SHORT).show()
            viewModel.loadWeatherFromFile(context, city)
            viewModel.loadForecastFromFile(context, city)
        }
    }


    val activity = context as? Activity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }


    val isTablet = isTablet(context)

    val useTabletLayout = isTablet &&
            (windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded ||
                    windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Medium)

    if (useTabletLayout) {
        TabletLayout(weather, viewModel, forecast)
    } else {
        PhoneLayout(weather, viewModel, forecast)
    }
}


@Composable
fun BasicWeatherFragment(
    weatherResponse: WeatherResponse?,
    viewModel: WeatherViewModel,
    modifier: Modifier
) {

    val unitSystem by viewModel.unitSystem
    val iconCode = weatherResponse?.weather?.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"

    weatherResponse?.let { weather ->
        // main panel with localisation and temperature and weather description
        Card(
            modifier = modifier
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

        Spacer(modifier = Modifier.height(16.dp))

        //second panel with time coords and preasure
        Card(
            modifier = modifier
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

@Composable
fun ExtraWeatherFragment(
    weatherResponse: WeatherResponse?,
    viewModel: WeatherViewModel,
    modifier: Modifier
) {

    val unitSystem by viewModel.unitSystem

    weatherResponse?.let { weather ->
        Card(
            modifier = modifier
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
                        modifier = modifier,
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

@Composable
fun ForecastFragment(
    forecastResponse: ForecastResponse?,
    viewModel: WeatherViewModel,
    modifier: Modifier
) {

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
fun Favourites(viewModel: WeatherViewModel) {

    val context = LocalContext.current
    val favoriteLocations by viewModel.favoriteLocations
    val currentCity by viewModel.currentCity

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 0.dp, bottom = 8.dp)
    ) {
        items(favoriteLocations) { city ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
//                            Toast.makeText(context, city, Toast.LENGTH_SHORT).show()
                            viewModel.setCity(city)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(city, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.removeFavoriteLocation(context, city) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń z ulubionych")
                    }
                }
            }
        }
    }
}


@Composable
fun Settings(viewModel: WeatherViewModel) {
    val context = LocalContext.current
    val unitSystem by viewModel.unitSystem
    val currentCity by viewModel.currentCity
    var cityInput by remember { mutableStateOf(currentCity) }

    val options = UnitSystem.entries

    // load and store interval
    var refreshInterval by remember {
        mutableStateOf(viewModel.loadRefreshInterval(context).toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
        ){
            Button(
                onClick = {
                    viewModel.addFavoriteLocation(context, cityInput)

                    viewModel.fetchWeather(cityInput, viewModel.unitSystem.value.apiValue, "pl") { weather ->
                        viewModel.saveWeatherToFile(context, cityInput, weather)
                    }

                    viewModel.fetchForecast(cityInput, viewModel.unitSystem.value.apiValue, "pl") { forecast ->
                        viewModel.saveForecastToFile(context, cityInput, forecast)
                    }

                    Toast.makeText(
                        context,
                        "$cityInput dodano do ulubionych i zapisano dane do pliku",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) {
                Text("Dodaj do ulubionych")
            }

            Button(
                onClick = {
                    viewModel.setCity(cityInput)
                    viewModel.fetchWeather(cityInput, viewModel.unitSystem.value.apiValue, "pl") {}
                    viewModel.fetchForecast(cityInput, viewModel.unitSystem.value.apiValue, "pl") {}
                },
            ) {
                Text("Zmień miasto",
                    textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Czas odświeżania (minuty):", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = refreshInterval,
            onValueChange = {
                refreshInterval = it.filter { c -> c.isDigit() }
            },
            label = { Text("Np. 60 (min. 15)") },
            modifier = Modifier.fillMaxWidth(),
            isError = refreshInterval.toIntOrNull()?.let { it < 15 } == true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val interval = refreshInterval.toIntOrNull()
                if (interval == null) {
                    Toast.makeText(context, "Podaj poprawną liczbę minut", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.saveRefreshInterval(context, interval)


                    WeatherUpdateWorker.schedulePeriodicWork(
                        context = context,
                        intervalMinutes = interval,
                        city = viewModel.currentCity.value,
                        unitSystem = viewModel.unitSystem.value.apiValue
                    )

                    Toast.makeText(
                        context,
                        "Ustawiono odświeżanie co $interval minut",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Zapisz interwał")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.fetchWeather(viewModel.currentCity.value, viewModel.unitSystem.value.apiValue, "pl") {}
                viewModel.fetchForecast(viewModel.currentCity.value, viewModel.unitSystem.value.apiValue, "pl") {}
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Odśwież teraz")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
fun NoInternetFooterChecker(viewModel: WeatherViewModel) {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(true) }
    var wasDisconnected by remember { mutableStateOf(false) }
    val city by viewModel.currentCity
    val unitSystem by viewModel.unitSystem

    LaunchedEffect(Unit) {
        while (true) {
            val currentConnectionState = NetworkChecker(context).isConnected()

            if (currentConnectionState && !isConnected) {
                viewModel.fetchWeather(
                    city,
                    units = unitSystem.apiValue,
                    "pl"
                ) { result ->
                    if (result != null) {
                        viewModel.saveLastWeatherData(context, result)
                    } else {
                        viewModel.loadLastWeatherData(context)
                    }
                }

                viewModel.fetchForecast(
                    city,
                    units = unitSystem.apiValue,
                    BuildConfig.WEATHER_API_KEY,
                ) { result ->
                    if (result != null) {
                        viewModel.saveLastForecastData(context, result)
                    } else {
                        viewModel.loadLastForecastData(context)

                    }
                }
            }

            wasDisconnected = !currentConnectionState
            isConnected = currentConnectionState

            delay(3000)
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
                text = "Brak połączenia z internetem. Dane mogą być nieaktualne i niepoprawne.",
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}