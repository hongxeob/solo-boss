package com.soloboss.ai.application.stats

import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import com.soloboss.ai.infrastructure.persistence.FollowUpTaskRepository
import com.soloboss.ai.infrastructure.persistence.IngestJobRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class StatsServiceTest {
    private val customerRepository = Mockito.mock(CustomerRepository::class.java)
    private val followUpTaskRepository = Mockito.mock(FollowUpTaskRepository::class.java)
    private val ingestJobRepository = Mockito.mock(IngestJobRepository::class.java)
    private val service = StatsService(customerRepository, followUpTaskRepository, ingestJobRepository)

    @Test
    fun `summarize calculates ai accuracy from auto saved ratio`() {
        val ownerId = UUID.randomUUID()

        Mockito.`when`(customerRepository.countByOwnerId(ownerId)).thenReturn(8)
        Mockito.`when`(followUpTaskRepository.countByOwnerId(ownerId)).thenReturn(3)
        Mockito.`when`(ingestJobRepository.countByOwnerId(ownerId)).thenReturn(10)
        Mockito.`when`(ingestJobRepository.countByOwnerIdAndStatus(ownerId, IngestJobStatus.AUTO_SAVED)).thenReturn(9)

        val result = service.summarize(ownerId)

        assertEquals(8, result.projectCount)
        assertEquals(90, result.aiAccuracy)
        assertEquals(0L, result.monthlyRevenue)
    }

    @Test
    fun `summarize returns zero accuracy when no ingest history`() {
        val ownerId = UUID.randomUUID()

        Mockito.`when`(customerRepository.countByOwnerId(ownerId)).thenReturn(2)
        Mockito.`when`(followUpTaskRepository.countByOwnerId(ownerId)).thenReturn(0)
        Mockito.`when`(ingestJobRepository.countByOwnerId(ownerId)).thenReturn(0)

        val result = service.summarize(ownerId)

        assertEquals(0, result.aiAccuracy)
        assertEquals(0L, result.monthlyRevenue)
    }
}
