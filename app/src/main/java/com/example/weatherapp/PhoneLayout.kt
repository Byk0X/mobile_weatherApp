package com.example.weatherapp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhoneLayout(
    weatherResponse: WeatherResponse?,
    viewModel: WeatherViewModel,
    forecastResponse: ForecastResponse?
) {
    val tabItems = listOf(
        TabItem.IconTab(Icons.Default.Home, "Podstawowe"),
        TabItem.IconTab(Icons.Default.DateRange, "Prognoza"),
        TabItem.IconTab(Icons.Default.Favorite, "Ulubione"),
        TabItem.IconTab(Icons.Rounded.Settings, "Ustawienia")
    )

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabItems.size })
    val coroutineScope = rememberCoroutineScope()

    Column {
        CenterAlignedTopAppBar(
            title = {
                Text(text = tabItems[pagerState.currentPage].label)
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            tabItems.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        when (tab) {
                            is TabItem.TextTab -> Text(tab.label)
                            is TabItem.IconTab -> Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        }
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
                    0 -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            BasicWeatherFragment(
                                weatherResponse,
                                viewModel,
                                Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            ExtraWeatherFragment(
                                weatherResponse,
                                viewModel,
                                Modifier.fillMaxWidth()
                            )
                        }
                    }

                    1 -> ForecastFragment(forecastResponse, viewModel, Modifier.fillMaxSize())
                    2 -> Favourites(viewModel)
                    3 -> Settings(viewModel)
                }
            }

            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                NoInternetFooterChecker(viewModel)
            }
        }
    }
}