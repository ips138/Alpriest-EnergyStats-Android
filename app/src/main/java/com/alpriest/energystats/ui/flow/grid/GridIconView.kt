package com.alpriest.energystats.ui.flow.grid

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.StringPower
import com.alpriest.energystats.ui.flow.home.GenerationViewModel
import com.alpriest.energystats.ui.flow.home.LoadedPowerFlowViewModel
import com.alpriest.energystats.ui.flow.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun GridIconView(viewModel: LoadedPowerFlowViewModel, iconHeight: Dp, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val decimalPlaces = themeStream.collectAsState().value.decimalPlaces
    val showGridTotals = themeStream.collectAsState().value.showGridTotals

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        PylonView(
            modifier = Modifier
                .height(iconHeight)
                .width(iconHeight * 1f)
                .clipToBounds(),
            themeStream = themeStream
        )

        if (showGridTotals) {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Row {
                    GridTotals(viewModel, decimalPlaces, themeStream)
                }
            } else {
                GridTotals(viewModel, decimalPlaces, themeStream)
            }
        }
    }
}

@Composable
private fun GridTotals(
    viewModel: LoadedPowerFlowViewModel,
    decimalPlaces: Int,
    themeStream: MutableStateFlow<AppTheme>
) {
    val displayUnit = themeStream.collectAsState().value.displayUnit
    val fontSize = themeStream.collectAsState().value.fontSize()
    val smallFontSize = themeStream.collectAsState().value.smallFontSize()
    val gridImportTotal = viewModel.gridImportTotal.collectAsState().value ?: 0.0 // TODO
    val gridExportTotal = viewModel.gridExportTotal.collectAsState().value ?: 0.0 // TODO

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = gridImportTotal.energy(displayUnit, decimalPlaces),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.total_import),
            fontSize = smallFontSize,
            color = Color.Gray,
        )

        Text(
            text = gridExportTotal.energy(displayUnit, decimalPlaces),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.total_export),
            fontSize = smallFontSize,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
fun GridIconViewPreview() {
    val loadedPowerFlowViewModel = LoadedPowerFlowViewModel(
        solar = 1.0,
        solarStrings = listOf(
            StringPower("pv1", 0.3),
            StringPower("pv2", 0.7)
        ),
        home = 2.45,
        grid = 2.45,
        todaysGeneration = GenerationViewModel(response = OpenHistoryResponse(deviceSN = "1", datas = listOf()), includeCT2 = false, invertCT2 = false),
        inverterTemperatures = null,
        hasBattery = true,
        battery = BatteryViewModel(),
        FakeConfigManager(),
        ct2 = 0.4,
        faults = listOf(),
        currentDevice = Device.preview(),
        network = DemoNetworking()
    )

    EnergyStatsTheme {
        GridIconView(
            loadedPowerFlowViewModel,
            iconHeight = 30.dp,
            themeStream = MutableStateFlow(AppTheme.demo().copy(showGridTotals = true)),
            modifier = Modifier
        )
    }
}
