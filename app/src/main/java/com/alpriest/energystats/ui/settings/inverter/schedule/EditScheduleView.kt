package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoFoxESSNetworking
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class EditScheduleView(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking,
    private val navController: NavHostController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(viewModel: EditScheduleViewModel = viewModel(factory = EditScheduleViewModelFactory(configManager, network, navController))) {
        val context = LocalContext.current
        val schedule = viewModel.scheduleStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state

        MonitorAlertDialog(viewModel)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.reason, onRetry = { viewModel.load(context) }, onLogout = { userManager.logout() })
            is LoadState.Inactive -> schedule?.let { Loaded(it, viewModel) }
        }
    }
}

@Composable
fun Loaded(schedule: Schedule, viewModel: EditScheduleViewModel) {
    val context = LocalContext.current
    val allowDeletion = viewModel.allowDeletionStream.collectAsState().value

    SettingsPage {
        ScheduleDetailView(schedule)

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.addTimePeriod() }) {
                Text("Add time period")
            }
            Button(onClick = { viewModel.autoFillScheduleGaps() }) {
                Text("Autofill gaps")
            }
            Button(onClick = { viewModel.saveSchedule(context) }) {
                Text("Activate schedule")
            }

            if (allowDeletion) {
                Button(onClick = { /* TODO */ }) {
                    Text("Delete schedule")
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun EditScheduleViewPreview() {
    EnergyStatsTheme {
        Loaded(
            schedule = Schedule.preview(),
            viewModel = EditScheduleViewModel(
                FakeConfigManager(),
                DemoFoxESSNetworking(),
                NavHostController(LocalContext.current)
            )
        )
    }
}

