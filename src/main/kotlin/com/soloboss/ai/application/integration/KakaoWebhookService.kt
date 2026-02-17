package com.soloboss.ai.application.integration

import com.soloboss.ai.application.ocr.OcrExtractCommand
import com.soloboss.ai.application.ocr.OcrExtractionService
import com.soloboss.ai.domain.interaction.SourceType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class KakaoWebhookService(
    private val ocrExtractionService: OcrExtractionService,
    private val channelOwnerResolver: ChannelOwnerResolver,
    private val kakaoSignatureVerifier: KakaoSignatureVerifier,
) {
    fun handle(command: KakaoWebhookCommand): KakaoWebhookResult {
        if (!kakaoSignatureVerifier.isValid(command.signature, command.eventId, command.messageId, command.channelId)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 카카오 시그니처입니다.")
        }

        val ownerId = channelOwnerResolver.resolveOwnerId(command.channelId)

        return ocrExtractionService.extract(
            OcrExtractCommand(
                ownerId = ownerId,
                idempotencyKey = "${command.channelId}:${command.messageId}",
                sourceType = command.messageType.toSourceType(),
                sourceUrl = command.mediaUrl,
                eventId = command.eventId,
                messageId = command.messageId,
                channelId = command.channelId,
                kakaoUserKey = command.kakaoUserKey,
            ),
        )
    }

    private fun String.toSourceType(): SourceType =
        when (lowercase()) {
            "image" -> SourceType.IMAGE
            "voice", "audio" -> SourceType.VOICE
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 message_type입니다: $this")
        }
}
