package com.soloboss.ai.application.consultation

import com.soloboss.ai.domain.customer.Customer
import com.soloboss.ai.domain.customer.CustomerSource
import com.soloboss.ai.domain.interaction.Consultation
import com.soloboss.ai.infrastructure.ai.ConsultationEmbeddingService
import com.soloboss.ai.infrastructure.persistence.ConsultationRepository
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class ConsultationServiceTest {
    private val consultationRepository = Mockito.mock(ConsultationRepository::class.java)
    private val customerRepository = Mockito.mock(CustomerRepository::class.java)
    private val embeddingService = Mockito.mock(ConsultationEmbeddingService::class.java)
    private val service = ConsultationService(consultationRepository, customerRepository, embeddingService)

    private val ownerId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()
    private val consultationId = UUID.randomUUID()

    @Test
    fun `create saves consultation and triggers embedding`() {
        val customer = customer(ownerId = ownerId)
        Mockito.`when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        Mockito.`when`(consultationRepository.save(any(Consultation::class.java))).thenAnswer {
            val c = it.arguments[0] as Consultation
            consultation(ownerId = c.ownerId, customerId = c.customerId, summary = c.summary)
        }

        val result =
            service.create(
                CreateConsultationCommand(
                    ownerId = ownerId,
                    customerId = customerId,
                    summary = "웹사이트 문의",
                    rawText = "안녕하세요",
                ),
            )

        assertEquals("웹사이트 문의", result.summary)
        Mockito.verify(consultationRepository).save(any(Consultation::class.java))

        val captor = ArgumentCaptor.forClass(UUID::class.java)
        Mockito.verify(embeddingService).embed(
            consultationId = captor.capture() ?: consultationId,
            ownerId = captor.capture() ?: ownerId,
            customerId = captor.capture() ?: customerId,
            summary = capture(ArgumentCaptor.forClass(String::class.java)) ?: "",
            rawText = captureNullable(ArgumentCaptor.forClass(String::class.java)),
            consultationDate = capture(ArgumentCaptor.forClass(OffsetDateTime::class.java)) ?: OffsetDateTime.now(),
        )
    }

    @Test
    fun `create throws when customer not found`() {
        Mockito.`when`(customerRepository.findById(customerId)).thenReturn(Optional.empty())

        assertThrows(EntityNotFoundException::class.java) {
            service.create(
                CreateConsultationCommand(
                    ownerId = ownerId,
                    customerId = customerId,
                    summary = "문의",
                ),
            )
        }
    }

    @Test
    fun `create throws when customer owner mismatch`() {
        val anotherOwnerId = UUID.randomUUID()
        Mockito.`when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer(ownerId = anotherOwnerId)))

        assertThrows(EntityNotFoundException::class.java) {
            service.create(
                CreateConsultationCommand(
                    ownerId = ownerId,
                    customerId = customerId,
                    summary = "문의",
                ),
            )
        }
    }

    @Test
    fun `create succeeds even when embedding fails`() {
        val customer = customer(ownerId = ownerId)
        Mockito.`when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))
        Mockito.`when`(consultationRepository.save(any(Consultation::class.java))).thenAnswer {
            val c = it.arguments[0] as Consultation
            consultation(ownerId = c.ownerId, customerId = c.customerId, summary = c.summary)
        }
        Mockito
            .doThrow(RuntimeException("OpenAI API 장애"))
            .`when`(embeddingService)
            .embed(
                consultationId = any(UUID::class.java) ?: consultationId,
                ownerId = any(UUID::class.java) ?: ownerId,
                customerId = any(UUID::class.java) ?: customerId,
                summary = any(String::class.java) ?: "",
                rawText = any(),
                consultationDate = any(OffsetDateTime::class.java) ?: OffsetDateTime.now(),
            )

        val result =
            service.create(
                CreateConsultationCommand(
                    ownerId = ownerId,
                    customerId = customerId,
                    summary = "문의",
                ),
            )

        assertEquals("문의", result.summary)
        Mockito.verify(consultationRepository).save(any(Consultation::class.java))
    }

    @Test
    fun `get throws when consultation owner mismatch`() {
        val anotherOwnerId = UUID.randomUUID()
        Mockito
            .`when`(consultationRepository.findById(consultationId))
            .thenReturn(Optional.of(consultation(ownerId = anotherOwnerId)))

        assertThrows(EntityNotFoundException::class.java) {
            service.get(ownerId = ownerId, consultationId = consultationId)
        }
    }

    @Test
    fun `update changes fields and triggers embedding`() {
        val existing = consultation(id = consultationId, ownerId = ownerId, summary = "원래 요약")
        Mockito.`when`(consultationRepository.findById(consultationId)).thenReturn(Optional.of(existing))

        val result =
            service.update(
                ownerId = ownerId,
                consultationId = consultationId,
                command = UpdateConsultationCommand(summary = "수정된 요약"),
            )

        assertEquals("수정된 요약", result.summary)
        Mockito.verify(embeddingService, Mockito.times(1)).embed(
            consultationId = any(UUID::class.java) ?: consultationId,
            ownerId = any(UUID::class.java) ?: ownerId,
            customerId = any(UUID::class.java) ?: customerId,
            summary = any(String::class.java) ?: "",
            rawText = any(),
            consultationDate = any(OffsetDateTime::class.java) ?: OffsetDateTime.now(),
        )
    }

    @Test
    fun `delete removes embedding and consultation`() {
        val existing = consultation(id = consultationId, ownerId = ownerId)
        Mockito.`when`(consultationRepository.findById(consultationId)).thenReturn(Optional.of(existing))

        service.delete(ownerId = ownerId, consultationId = consultationId)

        Mockito.verify(embeddingService).remove(consultationId)
        val captor = ArgumentCaptor.forClass(Consultation::class.java)
        Mockito.verify(consultationRepository).delete(captor.capture())
        assertEquals(consultationId, captor.value.id)
    }

    @Test
    fun `delete succeeds even when embedding removal fails`() {
        val existing = consultation(id = consultationId, ownerId = ownerId)
        Mockito.`when`(consultationRepository.findById(consultationId)).thenReturn(Optional.of(existing))
        Mockito.`when`(embeddingService.remove(consultationId)).thenThrow(RuntimeException("VectorStore 장애"))

        service.delete(ownerId = ownerId, consultationId = consultationId)

        Mockito.verify(consultationRepository).delete(existing)
    }

    private fun <T> capture(captor: ArgumentCaptor<T>): T = captor.capture()

    private fun <T> captureNullable(captor: ArgumentCaptor<T>): T? = captor.capture()

    private fun customer(
        id: UUID = customerId,
        ownerId: UUID,
    ): Customer =
        Customer(
            id = id,
            ownerId = ownerId,
            name = "테스트 고객",
            source = CustomerSource.MANUAL,
        )

    private fun consultation(
        id: UUID = consultationId,
        ownerId: UUID = this.ownerId,
        customerId: UUID = this.customerId,
        summary: String = "테스트 상담",
    ): Consultation =
        Consultation(
            id = id,
            ownerId = ownerId,
            customerId = customerId,
            summary = summary,
        )
}
