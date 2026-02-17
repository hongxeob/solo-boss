package com.soloboss.ai.infrastructure.persistence

import com.soloboss.ai.domain.interaction.IngestJob
import com.soloboss.ai.domain.interaction.IngestJobStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IngestJobRepository : JpaRepository<IngestJob, UUID> {
    fun countByOwnerId(ownerId: UUID): Long

    fun countByOwnerIdAndStatus(
        ownerId: UUID,
        status: IngestJobStatus,
    ): Long

    fun existsByIdempotencyKey(idempotencyKey: String): Boolean

    fun findByIdempotencyKey(idempotencyKey: String): IngestJob?

    fun findByOwnerIdAndStatus(
        ownerId: UUID,
        status: IngestJobStatus,
        pageable: Pageable,
    ): Page<IngestJob>
}
