package com.soloboss.ai.web.v1.stats

import com.soloboss.ai.application.stats.StatsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class RevenueByMonthResponse(
    val month: String,
    val amount: Long,
)

data class StatsResponse(
    val monthlyRevenue: Long,
    val projectCount: Long,
    val aiAccuracy: Int,
    val timeSaved: Int,
    val revenueByMonth: List<RevenueByMonthResponse>,
)

@RestController
@RequestMapping("/api/v1/stats")
class StatsController(
    private val statsService: StatsService,
) {
    @GetMapping
    fun getStats(
        @RequestParam ownerId: UUID,
    ): StatsResponse {
        val summary = statsService.summarize(ownerId)
        return StatsResponse(
            monthlyRevenue = summary.monthlyRevenue,
            projectCount = summary.projectCount,
            aiAccuracy = summary.aiAccuracy,
            timeSaved = summary.timeSaved,
            revenueByMonth = summary.revenueByMonth.map { RevenueByMonthResponse(it.month, it.amount) },
        )
    }
}
