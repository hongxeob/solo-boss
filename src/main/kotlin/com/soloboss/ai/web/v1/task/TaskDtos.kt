package com.soloboss.ai.web.v1.task

import com.soloboss.ai.application.task.TodayTaskItem
import com.soloboss.ai.domain.task.FollowUpTask
import com.soloboss.ai.domain.task.FollowUpTaskStatus
import java.time.OffsetDateTime
import java.util.UUID

data class TodayTaskResponse(
    val id: UUID,
    val clientName: String,
    val projectType: String,
    val suggestedMessage: String,
)

data class TaskActionResponse(
    val id: UUID,
    val status: FollowUpTaskStatus,
    val sentAt: OffsetDateTime?,
    val snoozedUntil: OffsetDateTime?,
)

data class SnoozeTaskRequest(
    val snoozedUntil: OffsetDateTime? = null,
)

fun TodayTaskItem.toResponse(): TodayTaskResponse =
    TodayTaskResponse(
        id = id,
        clientName = clientName,
        projectType = projectType,
        suggestedMessage = suggestedMessage,
    )

fun FollowUpTask.toActionResponse(): TaskActionResponse =
    TaskActionResponse(
        id = requireNotNull(id),
        status = status,
        sentAt = sentAt,
        snoozedUntil = snoozedUntil,
    )
