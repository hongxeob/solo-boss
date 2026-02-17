package com.soloboss.ai.application.integration

import com.soloboss.ai.application.notification.AlimtalkNotifier
import com.soloboss.ai.application.notification.AlimtalkTemplateCode
import com.soloboss.ai.application.ocr.OcrExtractCommand
import com.soloboss.ai.application.ocr.OcrExtractResult
import com.soloboss.ai.application.ocr.OcrExtractionService
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.SourceType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.util.UUID

class KakaoWebhookDuplicateTest {
    private val ocrExtractionService = Mockito.mock(OcrExtractionService::class.java)
    private val ownerResolver = Mockito.mock(ChannelOwnerResolver::class.java)
    private val signatureVerifier = Mockito.mock(KakaoSignatureVerifier::class.java)
    private val notifier = Mockito.mock(AlimtalkNotifier::class.java)
    private val service = KakaoWebhookService(ocrExtractionService, ownerResolver, signatureVerifier, notifier)

    @Test
    fun `duplicate webhook sends duplicate guidance template`() {
        val ownerId = UUID.randomUUID()
        Mockito.`when`(signatureVerifier.isValid("sig", "evt", "msg", "ch")).thenReturn(true)
        Mockito.`when`(ownerResolver.resolveOwnerId("ch")).thenReturn(ownerId)
        Mockito
            .`when`(
                ocrExtractionService.extract(
                    OcrExtractCommand(
                        ownerId = ownerId,
                        idempotencyKey = "ch:msg",
                        sourceType = SourceType.IMAGE,
                        sourceUrl = "https://example.com/image.jpg",
                        eventId = "evt",
                        messageId = "msg",
                        channelId = "ch",
                        kakaoUserKey = "kakao_1",
                    ),
                ),
            ).thenReturn(
                OcrExtractResult(
                    jobId = UUID.randomUUID(),
                    status = IngestJobStatus.AUTO_SAVED,
                    overallConfidence = 0.9,
                    extractionResult = null,
                    errorReason = null,
                ),
            )
        Mockito
            .`when`(
                ocrExtractionService.extract(
                    OcrExtractCommand(
                        ownerId = ownerId,
                        idempotencyKey = "ch:msg",
                        sourceType = SourceType.IMAGE,
                        sourceUrl = "https://example.com/image.jpg",
                        eventId = "evt",
                        messageId = "msg",
                        channelId = "ch",
                        kakaoUserKey = "kakao_1",
                    ),
                ),
            ).thenReturn(
                OcrExtractResult(
                    jobId = UUID.randomUUID(),
                    status = IngestJobStatus.AUTO_SAVED,
                    overallConfidence = 0.9,
                    extractionResult = null,
                    errorReason = null,
                ),
            )

        service.handle(
            KakaoWebhookCommand(
                eventId = "evt",
                messageId = "msg",
                channelId = "ch",
                kakaoUserKey = "kakao_1",
                messageType = "image",
                mediaUrl = "https://example.com/image.jpg",
                signature = "sig",
            ),
        )
        service.handle(
            KakaoWebhookCommand(
                eventId = "evt",
                messageId = "msg",
                channelId = "ch",
                kakaoUserKey = "kakao_1",
                messageType = "image",
                mediaUrl = "https://example.com/image.jpg",
                signature = "sig",
            ),
        )

        val captor = ArgumentCaptor.forClass(com.soloboss.ai.application.notification.AlimtalkSendCommand::class.java)
        Mockito.verify(notifier, Mockito.atLeast(2)).sendSafely(captor.capture())
        assert(captor.allValues.any { it.templateCode == AlimtalkTemplateCode.OCR_MULTI_IMAGE_ORDER })
        assertEquals(1, captor.allValues.count { it.templateCode == AlimtalkTemplateCode.RECEIVED_ACK })
    }
}
