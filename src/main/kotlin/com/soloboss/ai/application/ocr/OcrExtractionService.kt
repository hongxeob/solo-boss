package com.soloboss.ai.application.ocr

import com.soloboss.ai.domain.interaction.ExtractionResult
import com.soloboss.ai.domain.interaction.IngestJob
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.ReviewTask
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
                createReviewTaskIfNeeded(workingJob)
            }

            workingJob.toResult()
        } catch (ex: Exception) {
            ingestJob.errorReason = ex.message ?: "OCR 추출 실패"
            ingestJob.transitionTo(IngestJobStatus.FAILED)
            ingestJobRepository.save(ingestJob)
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

    private fun createReviewTaskIfNeeded(ingestJob: IngestJob) {
        val reviewRepository = reviewTaskRepository ?: return
        val ingestJobId = ingestJob.id ?: return
        if (reviewRepository.findByIngestJobId(ingestJobId) != null) {
            return
        }

        val extraction = ingestJob.extractionResult
        val uncertainFields = extraction?.findUncertainFields().orEmpty()

        reviewRepository.save(
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
