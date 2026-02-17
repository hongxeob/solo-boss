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
import org.mockito.Mockito
import java.util.UUID

class KakaoWebhookServiceTest {
    private val ocrExtractionService = Mockito.mock(OcrExtractionService::class.java)
    private val ownerResolver = Mockito.mock(ChannelOwnerResolver::class.java)
    private val signatureVerifier = Mockito.mock(KakaoSignatureVerifier::class.java)
    private val alimtalkNotifier = Mockito.mock(AlimtalkNotifier::class.java)
    private val service = KakaoWebhookService(ocrExtractionService, ownerResolver, signatureVerifier, alimtalkNotifier)

    @Test
    fun `handles webhook by triggering extraction with idempotency key`() {
        val ownerId = UUID.randomUUID()
        Mockito.`when`(signatureVerifier.isValid("sig", "evt_1", "msg_1", "ch_1")).thenReturn(true)
        Mockito.`when`(ownerResolver.resolveOwnerId("ch_1")).thenReturn(ownerId)
        val expectedCommand =
            OcrExtractCommand(
                ownerId = ownerId,
                idempotencyKey = "ch_1:msg_1",
                sourceType = SourceType.IMAGE,
                sourceUrl = "https://example.com/source.jpg",
                eventId = "evt_1",
                messageId = "msg_1",
                channelId = "ch_1",
                kakaoUserKey = "kakao_1",
            )
        Mockito.`when`(ocrExtractionService.extract(expectedCommand)).thenReturn(
            OcrExtractResult(
                jobId = UUID.randomUUID(),
                status = IngestJobStatus.AUTO_SAVED,
                overallConfidence = 0.91,
                extractionResult = null,
                errorReason = null,
            ),
        )

        val result =
            service.handle(
                KakaoWebhookCommand(
                    eventId = "evt_1",
                    messageId = "msg_1",
                    channelId = "ch_1",
                    kakaoUserKey = "kakao_1",
                    messageType = "image",
                    mediaUrl = "https://example.com/source.jpg",
                    signature = "sig",
                ),
            )

        assertEquals(IngestJobStatus.AUTO_SAVED, result.status)
        Mockito.verify(ocrExtractionService).extract(expectedCommand)
        val captor = org.mockito.ArgumentCaptor.forClass(com.soloboss.ai.application.notification.AlimtalkSendCommand::class.java)
        Mockito.verify(alimtalkNotifier).sendSafely(captor.capture())
        assertEquals(AlimtalkTemplateCode.RECEIVED_ACK, captor.value.templateCode)
    }
}
