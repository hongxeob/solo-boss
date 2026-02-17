package com.soloboss.ai.application.ocr

import com.soloboss.ai.domain.interaction.ConfidenceField
import com.soloboss.ai.domain.interaction.ExtractionResult
import com.soloboss.ai.domain.interaction.IngestJob
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.ReviewTask
import com.soloboss.ai.domain.interaction.SourceType
import com.soloboss.ai.domain.interaction.SummaryField
import com.soloboss.ai.infrastructure.persistence.IngestJobRepository
import com.soloboss.ai.infrastructure.persistence.ReviewTaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import java.util.UUID

class OcrExtractionServiceReviewTaskTest {
    private val ingestJobRepository = Mockito.mock(IngestJobRepository::class.java)
    private val reviewTaskRepository = Mockito.mock(ReviewTaskRepository::class.java)
    private val extractor = Mockito.mock(OcrExtractor::class.java)
    private val service = OcrExtractionService(ingestJobRepository, extractor, reviewTaskRepository)

    @Test
    fun `creates review task for low confidence fields`() {
        val ownerId = UUID.randomUUID()
        val ingestJobId = UUID.randomUUID()
        val command =
            OcrExtractCommand(
                ownerId = ownerId,
                idempotencyKey = "ch:msg",
                sourceType = SourceType.IMAGE,
                sourceUrl = "https://example.com/kakao.png",
            )
        Mockito.`when`(ingestJobRepository.findByIdempotencyKey(command.idempotencyKey)).thenReturn(null)
        Mockito.`when`(ingestJobRepository.save(any(IngestJob::class.java))).thenAnswer {
            val job = it.arguments[0] as IngestJob
            if (job.id == null) {
                IngestJob(
                    id = ingestJobId,
                    ownerId = job.ownerId,
                    eventId = job.eventId,
                    messageId = job.messageId,
                    channelId = job.channelId,
                    kakaoUserKey = job.kakaoUserKey,
                    sourceType = job.sourceType,
                    sourceUrl = job.sourceUrl,
                    idempotencyKey = job.idempotencyKey,
                    status = job.status,
                    overallConfidence = job.overallConfidence,
                    extractionResult = job.extractionResult,
                    ocrRawText = job.ocrRawText,
                    errorReason = job.errorReason,
                )
            } else {
                job
            }
        }
        Mockito.`when`(extractor.extract(command.sourceUrl)).thenReturn(
            OcrExtractionPayload(
                extractionResult =
                    ExtractionResult(
                        name = ConfidenceField("홍길동", 0.95),
                        phone = ConfidenceField("01012341234", 0.62),
                        email = ConfidenceField("a@test.com", 0.8),
                        projectType = ConfidenceField("웹", 0.5),
                        estimatedBudget = ConfidenceField("1000000", 0.75),
                        inquirySummary = SummaryField(listOf("요약1", "요약2", "요약3"), 0.6),
                        overallConfidence = 0.72,
                    ),
                ocrRawText = "원문",
            ),
        )
        Mockito.`when`(reviewTaskRepository.findByIngestJobId(ingestJobId)).thenReturn(null)

        val result = service.extract(command)

        assertEquals(IngestJobStatus.NEEDS_REVIEW, result.status)
        val captor = ArgumentCaptor.forClass(ReviewTask::class.java)
        Mockito.verify(reviewTaskRepository).save(captor.capture())
        assertEquals(ownerId, captor.value.ownerId)
        assertEquals(listOf("phone", "projectType", "inquirySummary"), captor.value.uncertainFields)
        assertNotNull(captor.value.expiresAt)
    }

    @Test
    fun `does not create review task for high confidence result`() {
        val ownerId = UUID.randomUUID()
        val command =
            OcrExtractCommand(
                ownerId = ownerId,
                idempotencyKey = "ch:msg2",
                sourceType = SourceType.IMAGE,
                sourceUrl = "https://example.com/high.png",
            )
        Mockito.`when`(ingestJobRepository.findByIdempotencyKey(command.idempotencyKey)).thenReturn(null)
        Mockito.`when`(ingestJobRepository.save(any(IngestJob::class.java))).thenAnswer { it.arguments[0] as IngestJob }
        Mockito.`when`(extractor.extract(command.sourceUrl)).thenReturn(
            OcrExtractionPayload(
                extractionResult = ExtractionResult(overallConfidence = 0.91),
                ocrRawText = "원문",
            ),
        )

        val result = service.extract(command)

        assertEquals(IngestJobStatus.AUTO_SAVED, result.status)
        Mockito.verifyNoInteractions(reviewTaskRepository)
    }
}
