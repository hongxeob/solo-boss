package com.soloboss.ai.application.ocr

import com.soloboss.ai.application.notification.AlimtalkNotifier
import com.soloboss.ai.application.notification.AlimtalkTemplateCode
import com.soloboss.ai.domain.interaction.IngestJob
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.SourceType
import com.soloboss.ai.infrastructure.persistence.IngestJobRepository
import com.soloboss.ai.infrastructure.persistence.ReviewTaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import java.util.UUID

class OcrQualityNotificationTest {
    private val ingestJobRepository = Mockito.mock(IngestJobRepository::class.java)
    private val reviewTaskRepository = Mockito.mock(ReviewTaskRepository::class.java)
    private val extractor = Mockito.mock(OcrExtractor::class.java)
    private val notifier = Mockito.mock(AlimtalkNotifier::class.java)
    private val service = OcrExtractionService(ingestJobRepository, extractor, reviewTaskRepository, notifier)

    @Test
    fun `sends blurry template when extractor throws blurry error`() {
        val ownerId = UUID.randomUUID()
        val command =
            OcrExtractCommand(
                ownerId = ownerId,
                idempotencyKey = "ch:err1",
                sourceType = SourceType.IMAGE,
                sourceUrl = "https://example.com/blur.jpg",
                kakaoUserKey = "kakao_1",
            )

        Mockito.`when`(ingestJobRepository.findByIdempotencyKey(command.idempotencyKey)).thenReturn(null)
        Mockito.`when`(ingestJobRepository.save(any(IngestJob::class.java))).thenAnswer { it.arguments[0] as IngestJob }
        Mockito.`when`(extractor.extract(command.sourceUrl)).thenThrow(RuntimeException("image too blurry"))

        val result = service.extract(command)

        assertEquals(IngestJobStatus.FAILED, result.status)
        val captor = ArgumentCaptor.forClass(com.soloboss.ai.application.notification.AlimtalkSendCommand::class.java)
        Mockito.verify(notifier).sendSafely(captor.capture())
        assertEquals(AlimtalkTemplateCode.OCR_IMAGE_BLURRY, captor.value.templateCode)
    }
}
