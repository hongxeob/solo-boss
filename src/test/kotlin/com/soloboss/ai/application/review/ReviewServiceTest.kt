package com.soloboss.ai.application.review

import com.soloboss.ai.domain.interaction.IngestJob
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.ReviewTask
import com.soloboss.ai.domain.interaction.ReviewTaskStatus
import com.soloboss.ai.domain.interaction.SourceType
import com.soloboss.ai.infrastructure.persistence.IngestJobRepository
import com.soloboss.ai.infrastructure.persistence.ReviewTaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.Optional
import java.util.UUID

class ReviewServiceTest {
    private val reviewTaskRepository = Mockito.mock(ReviewTaskRepository::class.java)
    private val ingestJobRepository = Mockito.mock(IngestJobRepository::class.java)
    private val service = ReviewService(reviewTaskRepository, ingestJobRepository)

    @Test
    fun `resolve marks review resolved and updates ingest status`() {
        val ownerId = UUID.randomUUID()
        val ingestId = UUID.randomUUID()
        val reviewId = UUID.randomUUID()
        val reviewTask =
            ReviewTask(
                id = reviewId,
                ownerId = ownerId,
                ingestJobId = ingestId,
                status = ReviewTaskStatus.OPEN,
            )
        val ingestJob =
            IngestJob(
                id = ingestId,
                ownerId = ownerId,
                sourceType = SourceType.IMAGE,
                idempotencyKey = "ch:msg",
                status = IngestJobStatus.NEEDS_REVIEW,
            )

        Mockito.`when`(reviewTaskRepository.findById(reviewId)).thenReturn(Optional.of(reviewTask))
        Mockito.`when`(ingestJobRepository.findById(ingestId)).thenReturn(Optional.of(ingestJob))

        val result = service.resolve(ownerId, reviewId, mapOf("name" to "홍길동"))

        assertEquals(ReviewTaskStatus.RESOLVED, result.status)
        assertEquals(IngestJobStatus.AUTO_SAVED, ingestJob.status)
    }
}
