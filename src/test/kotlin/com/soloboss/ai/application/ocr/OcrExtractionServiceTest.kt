package com.soloboss.ai.application.ocr

import com.soloboss.ai.domain.interaction.ExtractionResult
import com.soloboss.ai.domain.interaction.IngestJob
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.SourceType
import com.soloboss.ai.infrastructure.persistence.IngestJobRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import java.util.UUID

class OcrExtractionServiceTest {
    private val repository = Mockito.mock(IngestJobRepository::class.java)
    private val extractor = Mockito.mock(OcrExtractor::class.java)
    private val service = OcrExtractionService(repository, extractor)

    @Test
    fun `high confidence extraction becomes auto saved`() {
        val ownerId = UUID.randomUUID()
        val request =
            OcrExtractCommand(
                ownerId = ownerId,
                idempotencyKey = "ch_1:msg_1",
                sourceType = SourceType.IMAGE,
                sourceUrl = "https://example.com/sample.png",
            )

        Mockito.`when`(repository.findByIdempotencyKey(request.idempotencyKey)).thenReturn(null)
        Mockito.`when`(repository.save(any(IngestJob::class.java))).thenAnswer { it.arguments[0] as IngestJob }
        Mockito.`when`(extractor.extract(request.sourceUrl)).thenReturn(
            OcrExtractionPayload(
                extractionResult = ExtractionResult(overallConfidence = 0.92),
                ocrRawText = "원시 텍스트",
            ),
        )

        val result = service.extract(request)

        assertEquals(IngestJobStatus.AUTO_SAVED, result.status)
        val captor = ArgumentCaptor.forClass(IngestJob::class.java)
        Mockito.verify(repository, Mockito.atLeastOnce()).save(captor.capture())
        assertEquals(IngestJobStatus.AUTO_SAVED, captor.value.status)
        assertEquals(0.92, captor.value.overallConfidence)
    }

    @Test
    fun `low confidence extraction requires review`() {
        val ownerId = UUID.randomUUID()
        val request =
            OcrExtractCommand(
                ownerId = ownerId,
                idempotencyKey = "ch_2:msg_2",
                sourceType = SourceType.IMAGE,
                sourceUrl = "https://example.com/sample2.png",
            )

        Mockito.`when`(repository.findByIdempotencyKey(request.idempotencyKey)).thenReturn(null)
        Mockito.`when`(repository.save(any(IngestJob::class.java))).thenAnswer { it.arguments[0] as IngestJob }
        Mockito.`when`(extractor.extract(request.sourceUrl)).thenReturn(
            OcrExtractionPayload(
                extractionResult = ExtractionResult(overallConfidence = 0.70),
                ocrRawText = "원시 텍스트",
            ),
        )

        val result = service.extract(request)

        assertEquals(IngestJobStatus.NEEDS_REVIEW, result.status)
    }

    @Test
    fun `existing idempotency key reuses stored job`() {
        val ownerId = UUID.randomUUID()
        val request =
            OcrExtractCommand(
                ownerId = ownerId,
                idempotencyKey = "ch_3:msg_3",
                sourceType = SourceType.IMAGE,
                sourceUrl = "https://example.com/sample3.png",
            )
        val existing =
            IngestJob(
                ownerId = ownerId,
                sourceType = SourceType.IMAGE,
                idempotencyKey = request.idempotencyKey,
                status = IngestJobStatus.NEEDS_REVIEW,
                sourceUrl = request.sourceUrl,
            )

        Mockito.`when`(repository.findByIdempotencyKey(request.idempotencyKey)).thenReturn(existing)

        val result = service.extract(request)

        assertEquals(IngestJobStatus.NEEDS_REVIEW, result.status)
        Mockito.verify(extractor, Mockito.never()).extract(request.sourceUrl)
        Mockito.verify(repository, Mockito.never()).save(existing)
    }
}
