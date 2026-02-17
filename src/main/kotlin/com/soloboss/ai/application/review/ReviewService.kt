package com.soloboss.ai.application.review

import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.ReviewTask
import com.soloboss.ai.domain.interaction.ReviewTaskStatus
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
            ingestJob.transitionTo(IngestJobStatus.AUTO_SAVED)
        }

        return reviewTask
    }
}
