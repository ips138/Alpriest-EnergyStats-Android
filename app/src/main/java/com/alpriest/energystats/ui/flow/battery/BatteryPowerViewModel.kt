package com.alpriest.energystats.ui.flow.battery

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.stores.ConfigManaging

class BatteryPowerViewModel(
    private val configManager: ConfigManaging,
    private val actualStateOfCharge: Double,
    val chargePowerkWH: Double,
    val temperatures: List<Double>,
    val residual: Int
) : ViewModel() {
    private val calculator: BatteryCapacityCalculator = BatteryCapacityCalculator(
        capacityW = configManager.batteryCapacity.toDouble().toInt(),
        minimumSOC = configManager.minSOC
    )

    val batteryExtra: BatteryCapacityEstimate?
        get() {
            return calculator.batteryPercentageRemaining(
                batteryChargePowerkWH = chargePowerkWH,
                batteryStateOfCharge = actualStateOfCharge
            )
        }

    fun batteryStoredChargekWh(): Double {
        return calculator.currentEstimatedChargeAmountWh(batteryStateOfCharge = actualStateOfCharge, includeUnusableCapacity = !configManager.showUsableBatteryOnly) / 1000.0
    }

    fun batteryStateOfCharge(): Double {
        return calculator.effectiveBatteryStateOfCharge(batteryStateOfCharge = actualStateOfCharge, includeUnusableCapacity = !configManager.showUsableBatteryOnly)
    }

    fun setBatteryAsPercentage(value: Boolean) {
        configManager.showBatteryAsPercentage = value
    }

    val showUsableBatteryOnly: Boolean
        get() { return configManager.showUsableBatteryOnly }
}