package com.soloboss.ai.application.stats

import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import com.soloboss.ai.infrastructure.persistence.FollowUpTaskRepository
import com.soloboss.ai.infrastructure.persistence.IngestJobRepository
import org.springframework.stereotype.Service
import java.util.UUID

data class RevenueByMonth(
    val month: String,
    val amount: Long,
)

data class StatsSummary(
    val monthlyRevenue: Long,
    val projectCount: Long,
    val aiAccuracy: Int,
    val timeSaved: Int,
    val revenueByMonth: List<RevenueByMonth>,
)

@Service
class StatsService(
    private val customerRepository: CustomerRepository,
    private val followUpTaskRepository: FollowUpTaskRepository,
    private val ingestJobRepository: IngestJobRepository,
) {
    fun summarize(ownerId: UUID): StatsSummary {
        val customerCount = customerRepository.countByOwnerId(ownerId)
        val followUpCount = followUpTaskRepository.countByOwnerId(ownerId)
        val totalIngestCount = ingestJobRepository.countByOwnerId(ownerId)
        val autoSavedCount = ingestJobRepository.countByOwnerIdAndStatus(ownerId, IngestJobStatus.AUTO_SAVED)

        val aiAccuracy =
            if (totalIngestCount == 0L) {
                0
            } else {
                ((autoSavedCount.toDouble() / totalIngestCount.toDouble()) * 100).toInt()
            }

        return StatsSummary(
            monthlyRevenue = 0L,
            projectCount = customerCount,
            aiAccuracy = aiAccuracy,
            timeSaved = followUpCount.toInt(),
            revenueByMonth = emptyList(),
        )
    }
}
