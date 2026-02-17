package com.soloboss.ai.infrastructure.persistence

import com.soloboss.ai.domain.task.FollowUpTask
import com.soloboss.ai.domain.task.FollowUpTaskStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import java.util.UUID

interface FollowUpTaskRepository : JpaRepository<FollowUpTask, UUID> {
    fun findByOwnerIdAndStatus(
        ownerId: UUID,
        status: FollowUpTaskStatus,
        pageable: Pageable,
    ): Page<FollowUpTask>

    fun findByCustomerId(
        customerId: UUID,
        pageable: Pageable,
    ): Page<FollowUpTask>

    fun findByStatusAndScheduledAtBefore(
        status: FollowUpTaskStatus,
        scheduledAt: OffsetDateTime,
    ): List<FollowUpTask>
}
