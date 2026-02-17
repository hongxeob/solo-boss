package com.soloboss.ai.web.v1.task

import com.soloboss.ai.application.task.FollowUpTaskService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/v1/tasks")
class TaskController(
    private val followUpTaskService: FollowUpTaskService,
) {
    @GetMapping
    fun getTodayTasks(
        @RequestParam ownerId: UUID,
    ): List<TodayTaskResponse> =
        followUpTaskService
            .getTodayTasks(ownerId = ownerId)
            .map { it.toResponse() }

    @PostMapping("/{taskId}/send")
    fun sendTask(
        @PathVariable taskId: UUID,
        @RequestParam ownerId: UUID,
    ): TaskActionResponse =
        followUpTaskService
            .send(ownerId = ownerId, taskId = taskId)
            .toActionResponse()

    @PostMapping("/{taskId}/snooze")
    fun snoozeTask(
        @PathVariable taskId: UUID,
        @RequestParam ownerId: UUID,
        @RequestBody(required = false) request: SnoozeTaskRequest?,
    ): TaskActionResponse {
        val snoozedUntil = request?.snoozedUntil ?: OffsetDateTime.now().plusDays(1)
        return followUpTaskService
            .snooze(ownerId = ownerId, taskId = taskId, snoozedUntil = snoozedUntil)
            .toActionResponse()
    }
}
