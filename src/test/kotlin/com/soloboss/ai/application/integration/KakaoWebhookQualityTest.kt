package com.soloboss.ai.application.integration

import com.soloboss.ai.application.notification.AlimtalkNotifier
import com.soloboss.ai.application.notification.AlimtalkTemplateCode
import com.soloboss.ai.application.ocr.OcrExtractionService
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class KakaoWebhookQualityTest {
    private val ocrExtractionService = Mockito.mock(OcrExtractionService::class.java)
    private val ownerResolver = Mockito.mock(ChannelOwnerResolver::class.java)
    private val signatureVerifier = Mockito.mock(KakaoSignatureVerifier::class.java)
    private val notifier = Mockito.mock(AlimtalkNotifier::class.java)
    private val service = KakaoWebhookService(ocrExtractionService, ownerResolver, signatureVerifier, notifier)

    @Test
    fun `unsupported message type sends text only template`() {
        Mockito.`when`(signatureVerifier.isValid("sig", "evt", "msg", "ch")).thenReturn(true)
        Mockito.`when`(ownerResolver.resolveOwnerId("ch")).thenReturn(UUID.randomUUID())

        assertThrows(ResponseStatusException::class.java) {
            service.handle(
                KakaoWebhookCommand(
                    eventId = "evt",
                    messageId = "msg",
                    channelId = "ch",
                    kakaoUserKey = "kakao_1",
                    messageType = "text",
                    mediaUrl = "https://example.com",
                    signature = "sig",
                ),
            )
        }

        val captor = ArgumentCaptor.forClass(com.soloboss.ai.application.notification.AlimtalkSendCommand::class.java)
        Mockito.verify(notifier, Mockito.atLeastOnce()).sendSafely(captor.capture())
        assert(captor.allValues.any { it.templateCode == AlimtalkTemplateCode.OCR_TEXT_ONLY })
    }
}
