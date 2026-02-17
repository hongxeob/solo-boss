package com.soloboss.ai.application.review

import com.soloboss.ai.domain.customer.Customer
import com.soloboss.ai.domain.customer.CustomerSource
import com.soloboss.ai.domain.interaction.Consultation
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.ReviewTask
import com.soloboss.ai.domain.interaction.ReviewTaskStatus
import com.soloboss.ai.infrastructure.persistence.ConsultationRepository
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import com.soloboss.ai.infrastructure.persistence.IngestJobRepository
import com.soloboss.ai.infrastructure.persistence.ReviewTaskRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ReviewService(
    private val reviewTaskRepository: ReviewTaskRepository,
    private val ingestJobRepository: IngestJobRepository,
    private val customerRepository: CustomerRepository,
    private val consultationRepository: ConsultationRepository,
) {
    fun list(
        ownerId: UUID,
        status: ReviewTaskStatus?,
        pageable: Pageable,
    ): Page<ReviewTask> =
        if (status == null) {
            reviewTaskRepository.findByOwnerId(ownerId, pageable)
        } else {
            reviewTaskRepository.findByOwnerIdAndStatus(ownerId, status, pageable)
        }

    fun get(
        ownerId: UUID,
        reviewTaskId: UUID,
    ): ReviewTask {
        val reviewTask =
            reviewTaskRepository.findById(reviewTaskId).orElseThrow {
                EntityNotFoundException("검수 작업을 찾을 수 없습니다. reviewTaskId=$reviewTaskId")
            }
        if (reviewTask.ownerId != ownerId) {
            throw EntityNotFoundException("검수 작업을 찾을 수 없습니다. reviewTaskId=$reviewTaskId")
        }
        return reviewTask
    }

    @Transactional
    fun resolve(
        ownerId: UUID,
        reviewTaskId: UUID,
        correctedPayload: Map<String, Any?>,
    ): ReviewTask {
        val reviewTask = get(ownerId = ownerId, reviewTaskId = reviewTaskId)
        when (reviewTask.status) {
            ReviewTaskStatus.OPEN -> {
                reviewTask.transitionTo(ReviewTaskStatus.IN_PROGRESS)
                reviewTask.transitionTo(ReviewTaskStatus.RESOLVED)
            }

            ReviewTaskStatus.IN_PROGRESS -> reviewTask.transitionTo(ReviewTaskStatus.RESOLVED)
            ReviewTaskStatus.RESOLVED, ReviewTaskStatus.EXPIRED -> return reviewTask
        }

        reviewTask.proposedPayload = correctedPayload
        reviewTask.uncertainFields = emptyList()

        val ingestJob = ingestJobRepository.findById(reviewTask.ingestJobId).orElse(null)
        if (ingestJob != null && ingestJob.status == IngestJobStatus.NEEDS_REVIEW) {
            saveResolvedData(
                ownerId = ownerId,
                ingestJobId = reviewTask.ingestJobId,
                kakaoUserKey = ingestJob.kakaoUserKey,
                payload = correctedPayload,
            )
            ingestJob.transitionTo(IngestJobStatus.AUTO_SAVED)
        }

        return reviewTask
    }

    private fun saveResolvedData(
        ownerId: UUID,
        ingestJobId: UUID,
        kakaoUserKey: String?,
        payload: Map<String, Any?>,
    ) {
        val existingCustomer =
            kakaoUserKey
                ?.let { customerRepository.findByOwnerIdAndKakaoUserKey(ownerId, it) }

        val customer =
            existingCustomer ?: Customer(
                ownerId = ownerId,
                name = payload.stringValue("name") ?: "미확인 고객",
                kakaoUserKey = kakaoUserKey,
                source = if (kakaoUserKey != null) CustomerSource.KAKAO else CustomerSource.MANUAL,
            )

        payload.stringValue("name")?.let { customer.name = it }
        customer.phone = payload.stringValue("phone")
        customer.email = payload.stringValue("email")
        customer.projectType = payload.stringValue("projectType")
        customer.estimatedBudget = payload.stringValue("estimatedBudget")
        customer.inquirySummary = payload.summaryValue()

        val savedCustomer = customerRepository.save(customer)

        val existingConsultation = consultationRepository.findByIngestJobId(ingestJobId)
        if (existingConsultation != null) {
            existingConsultation.summary = payload.summaryValue() ?: existingConsultation.summary
            existingConsultation.rawText = payload.rawTextValue()
            return
        }

        consultationRepository.save(
            Consultation(
                ownerId = ownerId,
                customerId = requireNotNull(savedCustomer.id),
                ingestJobId = ingestJobId,
                summary = payload.summaryValue() ?: "검수 완료",
                rawText = payload.rawTextValue(),
            ),
        )
    }

    private fun Map<String, Any?>.stringValue(key: String): String? = this[key] as? String

    private fun Map<String, Any?>.summaryValue(): String? {
        val summary = this["inquirySummary"]
        return when (summary) {
            is String -> summary
            is List<*> -> summary.joinToString(" ") { it.toString() }
            else -> null
        }
    }

    private fun Map<String, Any?>.rawTextValue(): String? = this["rawText"] as? String
}
