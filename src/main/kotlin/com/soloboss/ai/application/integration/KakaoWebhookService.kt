package com.soloboss.ai.application.integration

import com.soloboss.ai.application.notification.AlimtalkNotifier
import com.soloboss.ai.application.notification.AlimtalkSendCommand
import com.soloboss.ai.application.notification.AlimtalkTemplateCode
import com.soloboss.ai.application.ocr.OcrExtractCommand
import com.soloboss.ai.application.ocr.OcrExtractionService
import com.soloboss.ai.domain.interaction.SourceType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class KakaoWebhookService(
    private val ocrExtractionService: OcrExtractionService,
    private val channelOwnerResolver: ChannelOwnerResolver,
    private val kakaoSignatureVerifier: KakaoSignatureVerifier,
    private val alimtalkNotifier: AlimtalkNotifier? = null,
) {
    fun handle(command: KakaoWebhookCommand): KakaoWebhookResult {
        if (!kakaoSignatureVerifier.isValid(command.signature, command.eventId, command.messageId, command.channelId)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 카카오 시그니처입니다.")
        }

        val ownerId = channelOwnerResolver.resolveOwnerId(command.channelId)
        val idempotencyKey = "${command.channelId}:${command.messageId}"
        val sourceType = command.messageType.toSourceTypeOrNotify(command.kakaoUserKey)

        alimtalkNotifier?.sendSafely(
            AlimtalkSendCommand(
                templateCode = AlimtalkTemplateCode.RECEIVED_ACK,
                to = command.kakaoUserKey,
                variables =
                    mapOf(
                        "eta_seconds" to DEFAULT_ETA_SECONDS.toString(),
                        "source_type" to command.messageType.toSourceTypeLabel(),
                        "received_at" to formatKst(OffsetDateTime.now()),
                        "job_status_link" to "${DEFAULT_JOB_STATUS_LINK_PREFIX}$idempotencyKey",
                    ),
            ),
        )

        return ocrExtractionService.extract(
            OcrExtractCommand(
                ownerId = ownerId,
                idempotencyKey = idempotencyKey,
                sourceType = sourceType,
                sourceUrl = command.mediaUrl,
                eventId = command.eventId,
                messageId = command.messageId,
                channelId = command.channelId,
                kakaoUserKey = command.kakaoUserKey,
            ),
        )
    }

    private fun String.toSourceTypeLabel(): String =
        when (lowercase()) {
            "image" -> "스크린샷"
            "voice", "audio" -> "음성"
            else -> "스크린샷"
        }

    private fun formatKst(now: OffsetDateTime): String =
        now
            .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
            .format(TIME_FORMATTER)

    private fun String.toSourceTypeOrNotify(kakaoUserKey: String): SourceType =
        when (lowercase()) {
            "image" -> SourceType.IMAGE
            "voice", "audio" -> SourceType.VOICE
            else -> {
                alimtalkNotifier?.sendSafely(
                    AlimtalkSendCommand(
                        templateCode = AlimtalkTemplateCode.OCR_TEXT_ONLY,
                        to = kakaoUserKey,
                        variables = emptyMap(),
                    ),
                )
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 message_type입니다: $this")
            }
        }

    companion object {
        private const val DEFAULT_ETA_SECONDS = 30
        private const val DEFAULT_JOB_STATUS_LINK_PREFIX = "https://soloboss.local/jobs/"
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
