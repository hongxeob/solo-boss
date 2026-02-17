package com.soloboss.ai.application.task

import com.soloboss.ai.domain.customer.Customer
import com.soloboss.ai.domain.customer.CustomerSource
import com.soloboss.ai.domain.task.FollowUpTask
import com.soloboss.ai.domain.task.FollowUpTaskStatus
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import com.soloboss.ai.infrastructure.persistence.FollowUpTaskRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class FollowUpTaskServiceTest {
    private val followUpTaskRepository = Mockito.mock(FollowUpTaskRepository::class.java)
    private val customerRepository = Mockito.mock(CustomerRepository::class.java)
    private val service = FollowUpTaskService(followUpTaskRepository, customerRepository)

    @Test
    fun `get today tasks maps customer and draft`() {
        val ownerId = UUID.randomUUID()
        val customerId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        val now = OffsetDateTime.now()

        val task =
            FollowUpTask(
                id = taskId,
                ownerId = ownerId,
                customerId = customerId,
                objective = "견적 재안내",
                draftContent = "안녕하세요, 지난 견적서 관련 문의드립니다.",
                status = FollowUpTaskStatus.DRAFT_READY,
                scheduledAt = now.minusHours(1),
            )
        val customer =
            Customer(
                id = customerId,
                ownerId = ownerId,
                name = "홍길동",
                projectType = "웹사이트 제작",
                source = CustomerSource.MANUAL,
            )

        Mockito
            .`when`(
                followUpTaskRepository.findByOwnerIdAndStatusInAndScheduledAtLessThanEqual(
                    ownerId,
                    setOf(FollowUpTaskStatus.DRAFT_READY, FollowUpTaskStatus.EDITING),
                    now,
                    PageRequest.of(0, 20),
                ),
            ).thenReturn(PageImpl(listOf(task)))
        Mockito.`when`(customerRepository.findAllById(setOf(customerId))).thenReturn(listOf(customer))

        val result = service.getTodayTasks(ownerId = ownerId, now = now)

        assertEquals(1, result.size)
        assertEquals(taskId, result.first().id)
        assertEquals("홍길동", result.first().clientName)
        assertEquals("웹사이트 제작", result.first().projectType)
    }

    @Test
    fun `send transitions task to sent`() {
        val ownerId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        val task =
            FollowUpTask(
                id = taskId,
                ownerId = ownerId,
                customerId = UUID.randomUUID(),
                objective = "리마인드",
                draftContent = "초안",
                status = FollowUpTaskStatus.DRAFT_READY,
                scheduledAt = OffsetDateTime.now().minusHours(1),
            )

        Mockito.`when`(followUpTaskRepository.findById(taskId)).thenReturn(Optional.of(task))

        val result = service.send(ownerId = ownerId, taskId = taskId)

        assertEquals(FollowUpTaskStatus.SENT, result.status)
        assertNotNull(result.sentAt)
    }

    @Test
    fun `snooze sets task to snoozed with due time`() {
        val ownerId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        val task =
            FollowUpTask(
                id = taskId,
                ownerId = ownerId,
                customerId = UUID.randomUUID(),
                objective = "리마인드",
                draftContent = "초안",
                status = FollowUpTaskStatus.DRAFT_READY,
                scheduledAt = OffsetDateTime.now().minusHours(1),
            )
        val snoozedUntil = OffsetDateTime.now().plusDays(1)

        Mockito.`when`(followUpTaskRepository.findById(taskId)).thenReturn(Optional.of(task))

        val result = service.snooze(ownerId = ownerId, taskId = taskId, snoozedUntil = snoozedUntil)

        assertEquals(FollowUpTaskStatus.SNOOZED, result.status)
        assertEquals(snoozedUntil, result.snoozedUntil)
    }

    @Test
    fun `send throws when task belongs to another owner`() {
        val taskId = UUID.randomUUID()
        Mockito.`when`(followUpTaskRepository.findById(taskId)).thenReturn(
            Optional.of(
                FollowUpTask(
                    id = taskId,
                    ownerId = UUID.randomUUID(),
                    customerId = UUID.randomUUID(),
                    objective = "리마인드",
                    status = FollowUpTaskStatus.DRAFT_READY,
                ),
            ),
        )

        assertThrows(EntityNotFoundException::class.java) {
            service.send(ownerId = UUID.randomUUID(), taskId = taskId)
        }
        Mockito.verify(followUpTaskRepository, Mockito.never()).save(any(FollowUpTask::class.java))
    }
}
