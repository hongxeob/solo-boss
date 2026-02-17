package com.soloboss.ai.application.notification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class AlimtalkServiceTest {
    private val gateway = Mockito.mock(AlimtalkSenderGateway::class.java)
    private val service = AlimtalkService(gateway)

    @Test
    fun `send throws bad request when required placeholder missing`() {
        val command =
            AlimtalkSendCommand(
                templateCode = AlimtalkTemplateCode.RECEIVED_ACK,
                to = "k_user_001",
                variables =
                    mapOf(
                        "eta_seconds" to "30",
                        "source_type" to "스크린샷",
                    ),
            )

        val ex =
            org.junit.jupiter.api
                .assertThrows<ResponseStatusException> { service.send(command) }

        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `send delegates to gateway when placeholders valid`() {
        val command =
            AlimtalkSendCommand(
                templateCode = AlimtalkTemplateCode.PROCESS_REVIEW_REQUIRED,
                to = "k_user_001",
                variables =
                    mapOf(
                        "review_count" to "2",
                        "customer_guess" to "홍길동",
                        "uncertain_fields" to "phone,followup_date",
                        "summary_one_line" to "요약",
                        "review_link" to "https://example.com/review/1",
                    ),
            )
        val expected =
            AlimtalkSendResult(
                templateCode = AlimtalkTemplateCode.PROCESS_REVIEW_REQUIRED,
                to = command.to,
                status = "MOCK_SENT",
                messageId = "mock-1",
            )
        Mockito.`when`(gateway.send(command)).thenReturn(expected)

        val result = service.send(command)

        assertEquals("MOCK_SENT", result.status)
        assertEquals("mock-1", result.messageId)
    }
}
