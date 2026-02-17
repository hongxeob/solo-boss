package com.soloboss.ai.infrastructure.persistence

import com.soloboss.ai.domain.interaction.ReviewTask
import com.soloboss.ai.domain.interaction.ReviewTaskStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import java.util.UUID

interface ReviewTaskRepository : JpaRepository<ReviewTask, UUID> {
    fun findByOwnerId(
        ownerId: UUID,
        pageable: Pageable,
    ): Page<ReviewTask>

    fun findByOwnerIdAndStatus(
        ownerId: UUID,
        status: ReviewTaskStatus,
        pageable: Pageable,
    ): Page<ReviewTask>

    fun findByIngestJobId(ingestJobId: UUID): ReviewTask?

    fun findByStatusAndExpiresAtBefore(
        status: ReviewTaskStatus,
        expiresAt: OffsetDateTime,
    ): List<ReviewTask>
}
