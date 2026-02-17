package com.soloboss.ai.application.review

import com.soloboss.ai.domain.customer.Customer
import com.soloboss.ai.domain.interaction.Consultation
import com.soloboss.ai.domain.interaction.IngestJob
import com.soloboss.ai.domain.interaction.IngestJobStatus
import com.soloboss.ai.domain.interaction.ReviewTask
import com.soloboss.ai.domain.interaction.ReviewTaskStatus
import com.soloboss.ai.domain.interaction.SourceType
import com.soloboss.ai.infrastructure.persistence.ConsultationRepository
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import com.soloboss.ai.infrastructure.persistence.IngestJobRepository
import com.soloboss.ai.infrastructure.persistence.ReviewTaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import java.util.Optional
import java.util.UUID

class ReviewServiceTest {
    private val reviewTaskRepository = Mockito.mock(ReviewTaskRepository::class.java)
    private val ingestJobRepository = Mockito.mock(IngestJobRepository::class.java)
    private val customerRepository = Mockito.mock(CustomerRepository::class.java)
    private val consultationRepository = Mockito.mock(ConsultationRepository::class.java)
    private val service = ReviewService(reviewTaskRepository, ingestJobRepository, customerRepository, consultationRepository)

    @Test
    fun `resolve marks review resolved and updates ingest status`() {
        val ownerId = UUID.randomUUID()
        val ingestId = UUID.randomUUID()
        val reviewId = UUID.randomUUID()
        val reviewTask =
            ReviewTask(
                id = reviewId,
                ownerId = ownerId,
                ingestJobId = ingestId,
                status = ReviewTaskStatus.OPEN,
            )
        val ingestJob =
            IngestJob(
                id = ingestId,
                ownerId = ownerId,
                sourceType = SourceType.IMAGE,
                idempotencyKey = "ch:msg",
                status = IngestJobStatus.NEEDS_REVIEW,
                kakaoUserKey = "kakao_1",
            )

        Mockito.`when`(reviewTaskRepository.findById(reviewId)).thenReturn(Optional.of(reviewTask))
        Mockito.`when`(ingestJobRepository.findById(ingestId)).thenReturn(Optional.of(ingestJob))
        Mockito.`when`(customerRepository.findByOwnerIdAndKakaoUserKey(ownerId, "kakao_1")).thenReturn(null)
        Mockito.`when`(customerRepository.save(org.mockito.ArgumentMatchers.any(Customer::class.java))).thenAnswer {
            val customer = it.arguments[0] as Customer
            if (customer.id == null) {
                Customer(
                    id = UUID.randomUUID(),
                    ownerId = customer.ownerId,
                    name = customer.name,
                    phone = customer.phone,
                    email = customer.email,
                    kakaoUserKey = customer.kakaoUserKey,
                    projectType = customer.projectType,
                    estimatedBudget = customer.estimatedBudget,
                    inquirySummary = customer.inquirySummary,
                    notes = customer.notes,
                    source = customer.source,
                )
            } else {
                customer
            }
        }
        Mockito.`when`(consultationRepository.findByIngestJobId(ingestId)).thenReturn(null)
        Mockito.`when`(consultationRepository.save(org.mockito.ArgumentMatchers.any(Consultation::class.java))).thenAnswer {
            it.arguments[0] as Consultation
        }

        val result = service.resolve(ownerId, reviewId, mapOf("name" to "홍길동", "inquirySummary" to "요약"))

        assertEquals(ReviewTaskStatus.RESOLVED, result.status)
        assertEquals(IngestJobStatus.AUTO_SAVED, ingestJob.status)
        val customerCaptor = ArgumentCaptor.forClass(Customer::class.java)
        Mockito.verify(customerRepository).save(customerCaptor.capture())
        assertEquals("홍길동", customerCaptor.value.name)
    }
}
