package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SchedulePhaseListItemView(phase: SchedulePhase, modifier: Modifier = Modifier) {
    val extra = when (phase.mode) {
        WorkMode.ForceDischarge -> " down to ${phase.forceDischargeSOC}% at ${phase.forceDischargePower}W"
        WorkMode.ForceCharge -> ""
        WorkMode.SelfUse -> " with ${phase.minSocOnGrid}% min SOC"
        else -> ""
    }

    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .height(IntrinsicSize.Max)
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(phase.color)
        )

        Spacer(Modifier.width(8.dp))

        Column {
            Row {
                Text(phase.start.formatted(), color = colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                Text(" - ", color = colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                Text(phase.end.formatted(), color = colorScheme.onSecondary, fontWeight = FontWeight.Bold)
            }

            Row {
                Text(phase.mode.title(), color = colorScheme.onSecondary.copy(alpha = 0.5f))
                Text(extra, color = colorScheme.onSecondary.copy(alpha = 0.5f))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SchedulePhaseListItemViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SchedulePhaseListItemView(
            phase = Schedule.preview().phases[0]
        )
    }
}
