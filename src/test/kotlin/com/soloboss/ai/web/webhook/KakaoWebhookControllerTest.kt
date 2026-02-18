package com.soloboss.ai.web.webhook

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.soloboss.ai.application.integration.KakaoWebhookCommand
import com.soloboss.ai.application.integration.KakaoWebhookService
import com.soloboss.ai.application.ocr.OcrExtractResult
import com.soloboss.ai.domain.interaction.IngestJobStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class KakaoWebhookControllerTest {
    private val service = Mockito.mock(KakaoWebhookService::class.java)
    private val controller = KakaoWebhookController(service)
    private val mapper = jacksonObjectMapper()

    @Test
    fun `header signature overrides body signature`() {
        val request =
            KakaoWebhookRequest(
                eventId = "evt_1",
                messageId = "msg_1",
                channelId = "ch_1",
                kakaoUserKey = "kakao_1",
                messageType = "image",
                mediaUrl = "https://example.com/image.jpg",
                signature = "body-signature",
            )
        val expected =
            KakaoWebhookCommand(
                eventId = "evt_1",
                messageId = "msg_1",
                channelId = "ch_1",
                kakaoUserKey = "kakao_1",
                messageType = "image",
                mediaUrl = "https://example.com/image.jpg",
                signature = "header-signature",
            )
        Mockito.`when`(service.handle(expected)).thenReturn(
            OcrExtractResult(
                jobId = UUID.randomUUID(),
                status = IngestJobStatus.RECEIVED,
                overallConfidence = null,
                extractionResult = null,
                errorReason = null,
            ),
        )

        val response = controller.handleWebhook("header-signature", null, request)

        assertEquals(IngestJobStatus.RECEIVED, response.status)
        Mockito.verify(service).handle(expected)
    }

    @Test
    fun `uses body signature when header missing`() {
        val request =
            KakaoWebhookRequest(
                eventId = "evt_2",
                messageId = "msg_2",
                channelId = "ch_2",
                kakaoUserKey = "kakao_2",
                messageType = "image",
                mediaUrl = "https://example.com/image2.jpg",
                signature = "body-signature",
            )
        val expected =
            KakaoWebhookCommand(
                eventId = "evt_2",
                messageId = "msg_2",
                channelId = "ch_2",
                kakaoUserKey = "kakao_2",
                messageType = "image",
                mediaUrl = "https://example.com/image2.jpg",
                signature = "body-signature",
            )
        Mockito.`when`(service.handle(expected)).thenReturn(
            OcrExtractResult(
                jobId = UUID.randomUUID(),
                status = IngestJobStatus.RECEIVED,
                overallConfidence = null,
                extractionResult = null,
                errorReason = null,
            ),
        )

        val response = controller.handleWebhook(null, null, request)

        assertEquals(IngestJobStatus.RECEIVED, response.status)
        Mockito.verify(service).handle(expected)
    }

    @Test
    fun `rejects request when no signature is present`() {
        val request =
            KakaoWebhookRequest(
                eventId = "evt_3",
                messageId = "msg_3",
                channelId = "ch_3",
                kakaoUserKey = "kakao_3",
                messageType = "image",
                mediaUrl = "https://example.com/image3.jpg",
                signature = null,
            )

        val ex =
            assertThrows(ResponseStatusException::class.java) {
                controller.handleWebhook(null, null, request)
            }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `deserializes snake_case payload`() {
        val json =
            """
            {
              "event_id": "evt_s",
              "message_id": "msg_s",
              "channel_id": "ch_s",
              "kakao_user_key": "kakao_s",
              "message_type": "image",
              "media_url": "https://example.com/s.jpg",
              "signature": "sig_s"
            }
            """.trimIndent()

        val request = mapper.readValue(json, KakaoWebhookRequest::class.java)

        assertEquals("evt_s", request.eventId)
        assertEquals("msg_s", request.messageId)
        assertEquals("ch_s", request.channelId)
        assertEquals("kakao_s", request.kakaoUserKey)
        assertEquals("image", request.messageType)
        assertEquals("https://example.com/s.jpg", request.mediaUrl)
        assertEquals("sig_s", request.signature)
    }
}
