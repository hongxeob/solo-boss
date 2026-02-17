package com.soloboss.ai.web.v1.review

import com.soloboss.ai.domain.interaction.ReviewTask
import com.soloboss.ai.domain.interaction.ReviewTaskStatus
import java.time.OffsetDateTime
import java.util.UUID

data class ResolveReviewRequest(
    val correctedPayload: Map<String, Any?> = emptyMap(),
)

data class ReviewResponse(
    val id: UUID,
    val ownerId: UUID,
    val ingestJobId: UUID,
    val customerGuess: String?,
    val uncertainFields: List<String>?,
    val proposedPayload: Map<String, Any?>?,
    val overallConfidence: Double?,
    val status: ReviewTaskStatus,
    val expiresAt: OffsetDateTime?,
    val resolvedAt: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

fun ReviewTask.toResponse(): ReviewResponse =
    ReviewResponse(
        id = requireNotNull(id),
        ownerId = ownerId,
        ingestJobId = ingestJobId,
        customerGuess = customerGuess,
        uncertainFields = uncertainFields,
        proposedPayload = proposedPayload,
        overallConfidence = overallConfidence,
        status = status,
        expiresAt = expiresAt,
        resolvedAt = resolvedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
