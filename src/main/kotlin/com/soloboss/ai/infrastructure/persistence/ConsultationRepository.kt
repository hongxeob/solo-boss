package com.soloboss.ai.infrastructure.persistence

import com.soloboss.ai.domain.interaction.Consultation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ConsultationRepository : JpaRepository<Consultation, UUID> {

    fun findByCustomerId(customerId: UUID, pageable: Pageable): Page<Consultation>

    fun findByOwnerIdAndCustomerId(ownerId: UUID, customerId: UUID, pageable: Pageable): Page<Consultation>

    fun findByIngestJobId(ingestJobId: UUID): Consultation?
}
