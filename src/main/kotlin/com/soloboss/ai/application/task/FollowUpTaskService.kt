package com.soloboss.ai.application.task

import com.soloboss.ai.domain.task.FollowUpTask
import com.soloboss.ai.domain.task.FollowUpTaskStatus
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import com.soloboss.ai.infrastructure.persistence.FollowUpTaskRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.util.UUID

data class TodayTaskItem(
    val id: UUID,
    val clientName: String,
    val projectType: String,
    val suggestedMessage: String,
)

@Service
@Transactional(readOnly = true)
class FollowUpTaskService(
    private val followUpTaskRepository: FollowUpTaskRepository,
    private val customerRepository: CustomerRepository,
) {
    fun getTodayTasks(
        ownerId: UUID,
        now: OffsetDateTime = OffsetDateTime.now(),
    ): List<TodayTaskItem> {
        val page =
            followUpTaskRepository.findByOwnerIdAndStatusInAndScheduledAtLessThanEqual(
                ownerId,
                setOf(FollowUpTaskStatus.DRAFT_READY, FollowUpTaskStatus.EDITING),
                now,
                PageRequest.of(0, 20),
            )

        val tasks = page.content
        if (tasks.isEmpty()) return emptyList()

        val customerMap = customerRepository.findAllById(tasks.map { it.customerId }.toSet()).associateBy { requireNotNull(it.id) }

        return tasks.map {
            val customer = customerMap[it.customerId]
            TodayTaskItem(
                id = requireNotNull(it.id),
                clientName = customer?.name ?: "알 수 없는 고객",
                projectType = customer?.projectType ?: "팔로업",
                suggestedMessage = it.draftContent ?: "",
            )
        }
    }

    @Transactional
    fun send(
        ownerId: UUID,
        taskId: UUID,
    ): FollowUpTask {
        val task = getOwnedTask(ownerId, taskId)
        if (task.status !in setOf(FollowUpTaskStatus.DRAFT_READY, FollowUpTaskStatus.EDITING)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "전송 가능한 상태가 아닙니다: ${task.status}")
        }
        task.transitionTo(FollowUpTaskStatus.SENT)
        return task
    }

    @Transactional
    fun snooze(
        ownerId: UUID,
        taskId: UUID,
        snoozedUntil: OffsetDateTime,
    ): FollowUpTask {
        val task = getOwnedTask(ownerId, taskId)
        if (task.status != FollowUpTaskStatus.DRAFT_READY) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "미루기 가능한 상태가 아닙니다: ${task.status}")
        }
        task.transitionTo(FollowUpTaskStatus.SNOOZED)
        task.snoozedUntil = snoozedUntil
        return task
    }

    private fun getOwnedTask(
        ownerId: UUID,
        taskId: UUID,
    ): FollowUpTask {
        val task =
            followUpTaskRepository.findById(taskId).orElseThrow {
                EntityNotFoundException("팔로업 작업을 찾을 수 없습니다. taskId=$taskId")
            }
        if (task.ownerId != ownerId) {
            throw EntityNotFoundException("팔로업 작업을 찾을 수 없습니다. taskId=$taskId")
        }
        return task
    }
}
