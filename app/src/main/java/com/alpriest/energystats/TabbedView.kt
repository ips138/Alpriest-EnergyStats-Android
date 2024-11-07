package com.alpriest.energystats

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.preview.FakeConfigStore
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.InMemoryLoggingNetworkStore
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.stores.SharedPreferencesCredentialStore
import com.alpriest.energystats.stores.WidgetDataSharer
import com.alpriest.energystats.stores.WidgetDataSharing
import com.alpriest.energystats.ui.flow.BannerAlertManager
import com.alpriest.energystats.ui.flow.BannerAlertManaging
import com.alpriest.energystats.ui.flow.PowerFlowTabView
import com.alpriest.energystats.ui.login.ConfigManager
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.NavigableParametersGraphTabView
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.NavigableSettingsView
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.statsgraph.StatsTabView
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
import com.alpriest.energystats.ui.summary.SummaryView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class TitleItem(
    val title: String,
    val icon: ImageVector,
    val isSettings: Boolean
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabbedView(
    configManager: ConfigManaging,
    network: Networking,
    userManager: UserManaging,
    onLogout: () -> Unit,
    themeStream: MutableStateFlow<AppTheme>,
    networkStore: InMemoryLoggingNetworkStore,
    onRateApp: () -> Unit,
    onBuyMeCoffee: () -> Unit,
    onWriteTempFile: (String, String) -> Uri?,
    filePathChooser: (filename: String, action: (Uri) -> Unit) -> Unit?,
    credentialStore: CredentialStore,
    solarForecastingProvider: () -> SolcastCaching,
    widgetDataSharer: WidgetDataSharing,
    bannerAlertManager: BannerAlertManaging
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val titles = listOf(
        TitleItem(stringResource(R.string.power_flow_tab), Icons.Default.SwapVert, false),
        TitleItem(stringResource(R.string.stats_tab), Icons.Default.BarChart, false),
        TitleItem("Parameters", Icons.Default.Insights, false),
        TitleItem("Summary", Icons.Default.MenuBook, false),
        TitleItem(stringResource(R.string.settings_tab), Icons.Default.Settings, true)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            HorizontalPager(
                modifier = Modifier.padding(padding),
                count = titles.size,
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> PowerFlowTabView(network, configManager, userManager, themeStream, widgetDataSharer, bannerAlertManager).Content(themeStream = themeStream)
                    1 -> StatsTabView(configManager, network, onWriteTempFile, filePathChooser, themeStream, userManager).Content()
                    2 -> NavigableParametersGraphTabView(configManager, userManager, network, onWriteTempFile, filePathChooser, themeStream).Content()
                    3 -> SummaryView(configManager, userManager, network, solarForecastingProvider).NavigableContent(themeStream = themeStream)
                    4 -> NavigableSettingsView(
                        config = configManager,
                        userManager = userManager,
                        onLogout = onLogout,
                        network = network,
                        networkStore = networkStore,
                        onRateApp = onRateApp,
                        onBuyMeCoffee = onBuyMeCoffee,
                        credentialStore = credentialStore,
                        solarForecastingProvider = solarForecastingProvider
                    )
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = darkenColor(colorScheme.background, 0.04f)
                ) {
                    titles.forEachIndexed { index, item ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.scrollToPage(
                                        index
                                    )
                                }
                            },
                            content = {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(imageVector = item.icon, contentDescription = null)
                                        Text(
                                            text = item.title,
                                            fontSize = 10.sp
                                        )
                                    }

                                    if (configManager.isDemoUser && item.isSettings) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.Red),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .padding(2.dp)
                                                .offset(x = 16.dp, y = 2.dp)
                                        ) {
                                            Text(
                                                "Demo",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .padding(horizontal = 2.dp)
                                                    .padding(bottom = 2.dp),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            },
                            selectedContentColor = colorScheme.primary,
                            unselectedContentColor = DimmedTextColor,
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomepagePreview() {
    val themeStream = MutableStateFlow(AppTheme.demo())
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        TabbedView(
            ConfigManager(
                config = FakeConfigStore(),
                networking = DemoNetworking(),
                appVersion = "1.19",
                themeStream = themeStream
            ),
            network = DemoNetworking(),
            userManager = FakeUserManager(),
            {},
            themeStream = themeStream,
            networkStore = InMemoryLoggingNetworkStore.shared,
            {},
            {},
            { _, _ -> null },
            { _, _ -> },
            SharedPreferencesCredentialStore(LocalContext.current.getSharedPreferences("com.alpriest.energystats", Context.MODE_PRIVATE)),
            { DemoSolarForecasting() },
            WidgetDataSharer(FakeConfigStore()),
            BannerAlertManager()
        )
    }
}

fun darkenColor(color: Color, percentage: Float): Color {
    val argb = color.toArgb()
    val alpha = argb ushr 24
    val red = argb shr 16 and 0xFF
    val green = argb shr 8 and 0xFF
    val blue = argb and 0xFF

    val darkenedRed = (red * (1 - percentage)).toInt()
    val darkenedGreen = (green * (1 - percentage)).toInt()
    val darkenedBlue = (blue * (1 - percentage)).toInt()

    return Color(alpha = alpha, red = darkenedRed, green = darkenedGreen, blue = darkenedBlue)
}
