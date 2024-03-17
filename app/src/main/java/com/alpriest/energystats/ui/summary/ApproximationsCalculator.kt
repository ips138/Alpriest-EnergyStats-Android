package com.alpriest.energystats.ui.summary

import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.parse
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.EnergyStatsFinancialModel
import com.alpriest.energystats.ui.flow.TotalsViewModel
import com.alpriest.energystats.ui.statsgraph.AbsoluteSelfSufficiencyCalculator
import com.alpriest.energystats.ui.statsgraph.ApproximationsViewModel
import com.alpriest.energystats.ui.statsgraph.NetSelfSufficiencyCalculator
import com.alpriest.energystats.ui.statsgraph.ReportType

class ApproximationsCalculator(
    private val configManager: ConfigManaging,
    private val networking: Networking
) {
    fun calculateApproximations(
        grid: Double,
        feedIn: Double,
        loads: Double,
        batteryCharge: Double,
        batteryDischarge: Double
    ): ApproximationsViewModel {
        val totalsViewModel = TotalsViewModel(grid, feedIn, loads, batteryCharge, batteryDischarge)

        val financialModel = EnergyStatsFinancialModel(totalsViewModel, configManager)

        val netResult = NetSelfSufficiencyCalculator().calculate(
            grid,
            feedIn,
            loads,
            batteryCharge,
            batteryDischarge
        )

        val absoluteResult = AbsoluteSelfSufficiencyCalculator().calculate(
            loads,
            grid
        )

        return ApproximationsViewModel(
            netSelfSufficiencyEstimate = "${netResult.first}%",
            netSelfSufficiencyEstimateCalculationBreakdown = netResult.second,
            absoluteSelfSufficiencyEstimate = "${absoluteResult.first}%",
            absoluteSelfSufficiencyEstimateCalculationBreakdown = absoluteResult.second,
            financialModel = financialModel,
            homeUsage = loads,
            totalsViewModel = totalsViewModel
        )
    }

    suspend fun generateTotals(
        deviceSN: String,
        reportData: List<OpenReportResponse>,
        reportType: ReportType,
        queryDate: QueryDate,
        reportVariables: List<ReportVariable>
    ): MutableMap<ReportVariable, Double> {
        val totals = mutableMapOf<ReportVariable, Double>()

        if (reportType == ReportType.day) {
            val reports = networking.fetchReport(deviceSN, reportVariables, queryDate, ReportType.month)
            reports.forEach { response ->
                ReportVariable.parse(response.variable).let {
                    totals[it] = response.values.first { it.index == queryDate.day }.value
                }
            }
        } else {
            reportData.forEach { response ->
                ReportVariable.parse(response.variable).let {
                    totals[it] = response.values.sumOf { kotlin.math.abs(it.value) }
                }
            }
        }

        return totals
    }
}