package com.soloboss.ai.application.ocr

import com.soloboss.ai.application.notification.AlimtalkNotifier
import com.soloboss.ai.application.notification.AlimtalkSendCommand
import com.soloboss.ai.application.notification.AlimtalkTemplateCode
import com.soloboss.ai.domain.interaction.ExtractionResult
import com.soloboss.ai.domain.interaction.IngestJob
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.ReviewTask
import com.soloboss.ai.domain.interaction.SourceType
import com.soloboss.ai.infrastructure.persistence.IngestJobRepository
import com.soloboss.ai.infrastructure.persistence.ReviewTaskRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional(readOnly = true)
class OcrExtractionService(
    private val ingestJobRepository: IngestJobRepository,
    private val ocrExtractor: OcrExtractor,
    private val reviewTaskRepository: ReviewTaskRepository? = null,
    private val alimtalkNotifier: AlimtalkNotifier? = null,
) {
    @Transactional
    fun extract(command: OcrExtractCommand): OcrExtractResult {
        val existing = ingestJobRepository.findByIdempotencyKey(command.idempotencyKey)
        if (existing != null) {
            return existing.toResult()
        }

        val ingestJob =
            IngestJob(
                ownerId = command.ownerId,
                eventId = command.eventId,
                messageId = command.messageId,
                channelId = command.channelId,
                kakaoUserKey = command.kakaoUserKey,
                sourceType = command.sourceType,
                sourceUrl = command.sourceUrl,
                idempotencyKey = command.idempotencyKey,
            )

        return try {
            val workingJob = ingestJobRepository.save(ingestJob)
            val payload = ocrExtractor.extract(command.sourceUrl)

            workingJob.transitionTo(IngestJobStatus.OCR_DONE)
            workingJob.ocrRawText = payload.ocrRawText
            workingJob.extractionResult = payload.extractionResult
            workingJob.overallConfidence = payload.extractionResult.overallConfidence

            workingJob.transitionTo(IngestJobStatus.STRUCTURED)
            val finalStatus =
                if (payload.extractionResult.overallConfidence >= AUTO_SAVE_THRESHOLD) {
                    IngestJobStatus.AUTO_SAVED
                } else {
                    IngestJobStatus.NEEDS_REVIEW
                }
            workingJob.transitionTo(finalStatus)
            ingestJobRepository.save(workingJob)

            if (finalStatus == IngestJobStatus.NEEDS_REVIEW) {
                val reviewTask = createReviewTaskIfNeeded(workingJob)
                sendReviewRequired(workingJob, reviewTask)
            } else if (finalStatus == IngestJobStatus.AUTO_SAVED) {
                sendAutoDone(workingJob)
            }

            workingJob.toResult()
        } catch (ex: Exception) {
            ingestJob.errorReason = ex.message ?: "OCR 추출 실패"
            ingestJob.transitionTo(IngestJobStatus.FAILED)
            ingestJobRepository.save(ingestJob)
            sendQualityIssueIfNeeded(command = command, exception = ex)
            ingestJob.toResult()
        }
    }

    fun get(
        ownerId: UUID,
        jobId: UUID,
    ): OcrExtractResult {
        val job =
            ingestJobRepository.findById(jobId).orElseThrow {
                EntityNotFoundException("수집 작업을 찾을 수 없습니다. jobId=$jobId")
            }
        if (job.ownerId != ownerId) {
            throw EntityNotFoundException("수집 작업을 찾을 수 없습니다. jobId=$jobId")
        }
        return job.toResult()
    }

    private fun createReviewTaskIfNeeded(ingestJob: IngestJob): ReviewTask? {
        val reviewRepository = reviewTaskRepository ?: return null
        val ingestJobId = ingestJob.id ?: return null
        val existing = reviewRepository.findByIngestJobId(ingestJobId)
        if (existing != null) {
            return existing
        }

        val extraction = ingestJob.extractionResult
        val uncertainFields = extraction?.findUncertainFields().orEmpty()

        return reviewRepository.save(
            ReviewTask(
                ownerId = ingestJob.ownerId,
                ingestJobId = ingestJobId,
                customerGuess = extraction?.name?.value,
                uncertainFields = uncertainFields,
                proposedPayload = extraction?.toProposedPayload(),
                overallConfidence = ingestJob.overallConfidence,
                expiresAt = OffsetDateTime.now().plusHours(REVIEW_EXPIRES_HOURS),
            ),
        )
    }

    private fun sendAutoDone(ingestJob: IngestJob) {
        val to = ingestJob.kakaoUserKey ?: return
        val extraction = ingestJob.extractionResult ?: return
        alimtalkNotifier?.sendSafely(
            AlimtalkSendCommand(
                templateCode = AlimtalkTemplateCode.PROCESS_AUTO_DONE,
                to = to,
                variables =
                    mapOf(
                        "customer_name" to (extraction.name.value ?: "미확인 고객"),
                        "summary_one_line" to extraction.toSummaryOneLine(),
                        "followup_at" to "미정",
                        "customer_card_link" to "https://soloboss.local/customers",
                        "followup_draft_link" to "https://soloboss.local/followups",
                    ),
            ),
        )
    }

    private fun sendReviewRequired(
        ingestJob: IngestJob,
        reviewTask: ReviewTask?,
    ) {
        val to = ingestJob.kakaoUserKey ?: return
        val extraction = ingestJob.extractionResult ?: return
        val uncertainFields = reviewTask?.uncertainFields.orEmpty()
        val reviewLink = reviewTask?.id?.let { "https://soloboss.local/reviews/$it" } ?: "https://soloboss.local/reviews"

        alimtalkNotifier?.sendSafely(
            AlimtalkSendCommand(
                templateCode = AlimtalkTemplateCode.PROCESS_REVIEW_REQUIRED,
                to = to,
                variables =
                    mapOf(
                        "review_count" to uncertainFields.size.toString(),
                        "customer_guess" to (extraction.name.value ?: "미확인"),
                        "uncertain_fields" to uncertainFields.joinToString(","),
                        "summary_one_line" to extraction.toSummaryOneLine(),
                        "review_link" to reviewLink,
                    ),
            ),
        )
    }

    private fun sendQualityIssueIfNeeded(
        command: OcrExtractCommand,
        exception: Exception,
    ) {
        val to = command.kakaoUserKey ?: return
        val templateCode = classifyQualityIssue(command.sourceType, exception.message.orEmpty())
        alimtalkNotifier?.sendSafely(
            AlimtalkSendCommand(
                templateCode = templateCode,
                to = to,
                variables = emptyMap(),
            ),
        )
    }

    private fun classifyQualityIssue(
        sourceType: SourceType,
        message: String,
    ): AlimtalkTemplateCode {
        if (sourceType == SourceType.VOICE) {
            return AlimtalkTemplateCode.OCR_TEXT_ONLY
        }
        val lower = message.lowercase()
        return when {
            "blur" in lower || "blurry" in lower -> AlimtalkTemplateCode.OCR_IMAGE_BLURRY
            "exposure" in lower || "bright" in lower || "dark" in lower -> AlimtalkTemplateCode.OCR_IMAGE_EXPOSURE
            "conversation" in lower || "chat" in lower -> AlimtalkTemplateCode.OCR_NOT_CONVERSATION
            "multi" in lower || "order" in lower -> AlimtalkTemplateCode.OCR_MULTI_IMAGE_ORDER
            else -> AlimtalkTemplateCode.OCR_IMAGE_BLURRY
        }
    }

    private fun ExtractionResult.findUncertainFields(): List<String> =
        buildList {
            if (name.confidence < FIELD_REVIEW_THRESHOLD) add("name")
            if (phone.confidence < FIELD_REVIEW_THRESHOLD) add("phone")
            if (email.confidence < FIELD_REVIEW_THRESHOLD) add("email")
            if (projectType.confidence < FIELD_REVIEW_THRESHOLD) add("projectType")
            if (estimatedBudget.confidence < FIELD_REVIEW_THRESHOLD) add("estimatedBudget")
            if (inquirySummary.confidence < FIELD_REVIEW_THRESHOLD) add("inquirySummary")
        }

    private fun ExtractionResult.toProposedPayload(): Map<String, Any?> =
        mapOf(
            "name" to name.value,
            "phone" to phone.value,
            "email" to email.value,
            "projectType" to projectType.value,
            "estimatedBudget" to estimatedBudget.value,
            "inquirySummary" to inquirySummary.lines,
        )

    private fun ExtractionResult.toSummaryOneLine(): String = inquirySummary.lines.firstOrNull().orEmpty()

    private fun IngestJob.toResult(): OcrExtractResult =
        OcrExtractResult(
            jobId = id,
            status = status,
            overallConfidence = overallConfidence,
            extractionResult = extractionResult,
            errorReason = errorReason,
        )

    companion object {
        private const val AUTO_SAVE_THRESHOLD = 0.85
        private const val FIELD_REVIEW_THRESHOLD = 0.7
        private const val REVIEW_EXPIRES_HOURS = 24L
    }
}
