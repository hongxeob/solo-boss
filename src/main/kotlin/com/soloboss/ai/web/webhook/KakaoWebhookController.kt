package com.soloboss.ai.web.webhook

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.soloboss.ai.application.integration.KakaoWebhookCommand
import com.soloboss.ai.application.integration.KakaoWebhookService
import com.soloboss.ai.web.v1.ocr.OcrExtractResponse
import com.soloboss.ai.web.v1.ocr.toResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class KakaoWebhookRequest(
    @field:NotBlank
    @field:JsonProperty("event_id")
    @field:JsonAlias("eventId")
    val eventId: String?,
    @field:NotBlank
    @field:JsonProperty("message_id")
    @field:JsonAlias("messageId")
    val messageId: String?,
    @field:NotBlank
    @field:JsonProperty("channel_id")
    @field:JsonAlias("channelId")
    val channelId: String?,
    @field:NotBlank
    @field:JsonProperty("kakao_user_key")
    @field:JsonAlias("kakaoUserKey")
    val kakaoUserKey: String?,
    @field:NotBlank
    @field:JsonProperty("message_type")
    @field:JsonAlias("messageType")
    val messageType: String?,
    @field:NotBlank
    @field:JsonProperty("media_url")
    @field:JsonAlias("mediaUrl")
    val mediaUrl: String?,
    @field:JsonProperty("sent_at")
    @field:JsonAlias("sentAt")
    val sentAt: String? = null,
    @field:JsonProperty("raw_payload")
    @field:JsonAlias("rawPayload")
    val rawPayload: Map<String, Any?>? = null,
    val signature: String? = null,
)

@RestController
@RequestMapping("/api/v1/integrations/kakao")
class KakaoWebhookController(
    private val kakaoWebhookService: KakaoWebhookService,
) {
    @PostMapping("/webhook")
    fun handleWebhook(
        @RequestHeader(name = "X-Kakao-Signature", required = false) kakaoSignatureHeader: String?,
        @RequestHeader(name = "X-Signature", required = false) genericSignatureHeader: String?,
        @Valid @RequestBody request: KakaoWebhookRequest,
    ): OcrExtractResponse {
        val signature =
            listOf(kakaoSignatureHeader, genericSignatureHeader, request.signature)
                .firstOrNull { !it.isNullOrBlank() }
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "시그니처가 없습니다.")

        return kakaoWebhookService
            .handle(
                KakaoWebhookCommand(
                    eventId = requireNotNull(request.eventId),
                    messageId = requireNotNull(request.messageId),
                    channelId = requireNotNull(request.channelId),
                    kakaoUserKey = requireNotNull(request.kakaoUserKey),
                    messageType = requireNotNull(request.messageType),
                    mediaUrl = requireNotNull(request.mediaUrl),
                    signature = signature,
                ),
            ).toResponse()
    }
}
