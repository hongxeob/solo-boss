package com.soloboss.ai.web.v1.ocr

import com.soloboss.ai.application.ocr.OcrExtractCommand
import com.soloboss.ai.application.ocr.OcrExtractResult
import com.soloboss.ai.domain.interaction.ExtractionResult
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.SourceType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class OcrExtractRequest(
    @field:NotNull
    val ownerId: UUID?,
    @field:NotBlank
    val idempotencyKey: String?,
    @field:NotNull
    val sourceType: SourceType?,
    @field:NotBlank
    val sourceUrl: String?,
    val eventId: String? = null,
    val messageId: String? = null,
    val channelId: String? = null,
    val kakaoUserKey: String? = null,
) {
    fun toCommand(): OcrExtractCommand =
        OcrExtractCommand(
            ownerId = requireNotNull(ownerId),
            idempotencyKey = requireNotNull(idempotencyKey),
            sourceType = requireNotNull(sourceType),
            sourceUrl = requireNotNull(sourceUrl),
            eventId = eventId,
            messageId = messageId,
            channelId = channelId,
            kakaoUserKey = kakaoUserKey,
        )
}

data class OcrExtractResponse(
    val jobId: UUID?,
    val status: IngestJobStatus,
    val overallConfidence: Double?,
    val extractionResult: ExtractionResult?,
    val errorReason: String?,
)

fun OcrExtractResult.toResponse(): OcrExtractResponse =
    OcrExtractResponse(
        jobId = jobId,
        status = status,
        overallConfidence = overallConfidence,
        extractionResult = extractionResult,
        errorReason = errorReason,
    )
