package com.soloboss.ai.application.consultation

import com.soloboss.ai.domain.interaction.Consultation
import com.soloboss.ai.infrastructure.persistence.ConsultationRepository
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ConsultationService(
    private val consultationRepository: ConsultationRepository,
    private val customerRepository: CustomerRepository,
) {
    @Transactional
    fun create(command: CreateConsultationCommand): Consultation {
        val customer =
            customerRepository.findById(command.customerId).orElseThrow {
                EntityNotFoundException("고객을 찾을 수 없습니다. customerId=${command.customerId}")
            }
        if (customer.ownerId != command.ownerId) {
            throw EntityNotFoundException("고객을 찾을 수 없습니다. customerId=${command.customerId}")
        }

        return consultationRepository.save(
            Consultation(
                ownerId = command.ownerId,
                customerId = command.customerId,
                summary = command.summary,
                rawText = command.rawText,
                consultationDate = command.consultationDate ?: OffsetDateTime.now(),
            ),
        )
    }

    fun get(
        ownerId: UUID,
        consultationId: UUID,
    ): Consultation {
        val consultation =
            consultationRepository.findById(consultationId).orElseThrow {
                EntityNotFoundException("상담 기록을 찾을 수 없습니다. consultationId=$consultationId")
            }
        if (consultation.ownerId != ownerId) {
            throw EntityNotFoundException("상담 기록을 찾을 수 없습니다. consultationId=$consultationId")
        }
        return consultation
    }

    fun listByCustomer(
        ownerId: UUID,
        customerId: UUID,
        pageable: Pageable,
    ): Page<Consultation> = consultationRepository.findByOwnerIdAndCustomerId(ownerId, customerId, pageable)

    fun list(
        ownerId: UUID,
        pageable: Pageable,
    ): Page<Consultation> = consultationRepository.findByOwnerId(ownerId, pageable)

    @Transactional
    fun update(
        ownerId: UUID,
        consultationId: UUID,
        command: UpdateConsultationCommand,
    ): Consultation {
        val consultation = get(ownerId = ownerId, consultationId = consultationId)

        command.summary?.let { consultation.summary = it }
        command.rawText?.let { consultation.rawText = it }
        command.consultationDate?.let { consultation.consultationDate = it }

        return consultation
    }

    @Transactional
    fun delete(
        ownerId: UUID,
        consultationId: UUID,
    ) {
        val consultation = get(ownerId = ownerId, consultationId = consultationId)
        consultationRepository.delete(consultation)
    }
}
