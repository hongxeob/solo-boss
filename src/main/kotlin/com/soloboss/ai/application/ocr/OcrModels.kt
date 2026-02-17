package com.soloboss.ai.application.ocr

import com.soloboss.ai.domain.interaction.ExtractionResult
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.SourceType
import java.util.UUID

data class OcrExtractCommand(
    val ownerId: UUID,
    val idempotencyKey: String,
    val sourceType: SourceType,
    val sourceUrl: String,
    val eventId: String? = null,
    val messageId: String? = null,
    val channelId: String? = null,
    val kakaoUserKey: String? = null,
)

data class OcrExtractionPayload(
    val extractionResult: ExtractionResult,
    val ocrRawText: String,
)

data class OcrExtractResult(
    val jobId: UUID?,
    val status: IngestJobStatus,
    val overallConfidence: Double?,
    val extractionResult: ExtractionResult?,
    val errorReason: String?,
)

fun interface OcrExtractor {
    fun extract(sourceUrl: String): OcrExtractionPayload
}
