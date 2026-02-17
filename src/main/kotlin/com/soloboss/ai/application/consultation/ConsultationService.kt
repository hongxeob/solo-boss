package com.soloboss.ai.application.consultation

import com.soloboss.ai.domain.interaction.Consultation
import com.soloboss.ai.infrastructure.ai.ConsultationEmbeddingService
import com.soloboss.ai.infrastructure.persistence.ConsultationRepository
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
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
    private val embeddingService: ConsultationEmbeddingService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun create(command: CreateConsultationCommand): Consultation {
        val customer =
            customerRepository.findById(command.customerId).orElseThrow {
                EntityNotFoundException("고객을 찾을 수 없습니다. customerId=${command.customerId}")
            }
        if (customer.ownerId != command.ownerId) {
            throw EntityNotFoundException("고객을 찾을 수 없습니다. customerId=${command.customerId}")
        }

        val saved =
            consultationRepository.save(
                Consultation(
                    ownerId = command.ownerId,
                    customerId = command.customerId,
                    summary = command.summary,
                    rawText = command.rawText,
                    consultationDate = command.consultationDate ?: OffsetDateTime.now(),
                ),
            )
        embedSafely(saved)
        return saved
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

        embedSafely(consultation)
        return consultation
    }

    @Transactional
    fun delete(
        ownerId: UUID,
        consultationId: UUID,
    ) {
        val consultation = get(ownerId = ownerId, consultationId = consultationId)
        try {
            embeddingService.remove(consultationId)
        } catch (e: Exception) {
            logger.warn("임베딩 삭제 실패: consultationId={}", consultationId, e)
        }
        consultationRepository.delete(consultation)
    }

    /**
     * 임베딩 저장을 안전하게 수행한다.
     * OpenAI API 장애 시에도 CRUD는 정상 동작을 보장한다.
     */
    private fun embedSafely(consultation: Consultation) {
        try {
            embeddingService.embed(
                consultationId = requireNotNull(consultation.id),
                ownerId = consultation.ownerId,
                customerId = consultation.customerId,
                summary = consultation.summary,
                rawText = consultation.rawText,
                consultationDate = consultation.consultationDate,
            )
        } catch (e: Exception) {
            logger.warn("임베딩 저장 실패: consultationId={}", consultation.id, e)
        }
    }
}
