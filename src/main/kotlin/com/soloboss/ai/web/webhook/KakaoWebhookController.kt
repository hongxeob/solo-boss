package com.soloboss.ai.web.webhook

import com.soloboss.ai.application.integration.KakaoWebhookCommand
import com.soloboss.ai.application.integration.KakaoWebhookService
import com.soloboss.ai.web.v1.ocr.OcrExtractResponse
import com.soloboss.ai.web.v1.ocr.toResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class KakaoWebhookRequest(
    @field:NotBlank
    val eventId: String?,
    @field:NotBlank
    val messageId: String?,
    @field:NotBlank
    val channelId: String?,
    @field:NotBlank
    val kakaoUserKey: String?,
    @field:NotBlank
    val messageType: String?,
    @field:NotBlank
    val mediaUrl: String?,
    @field:NotBlank
    val signature: String?,
)

@RestController
@RequestMapping("/api/v1/integrations/kakao")
class KakaoWebhookController(
    private val kakaoWebhookService: KakaoWebhookService,
) {
    @PostMapping("/webhook")
    fun handleWebhook(
        @Valid @RequestBody request: KakaoWebhookRequest,
    ): OcrExtractResponse =
        kakaoWebhookService
            .handle(
                KakaoWebhookCommand(
                    eventId = requireNotNull(request.eventId),
                    messageId = requireNotNull(request.messageId),
                    channelId = requireNotNull(request.channelId),
                    kakaoUserKey = requireNotNull(request.kakaoUserKey),
                    messageType = requireNotNull(request.messageType),
                    mediaUrl = requireNotNull(request.mediaUrl),
                    signature = requireNotNull(request.signature),
                ),
            ).toResponse()
}
