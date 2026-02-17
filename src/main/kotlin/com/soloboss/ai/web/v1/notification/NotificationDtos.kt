package com.soloboss.ai.web.v1.notification

import com.soloboss.ai.application.notification.AlimtalkSendCommand
import com.soloboss.ai.application.notification.AlimtalkSendResult
import com.soloboss.ai.application.notification.AlimtalkTemplateCode
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class AlimtalkSendRequest(
    @field:NotNull
    val templateCode: AlimtalkTemplateCode?,
    @field:NotBlank
    val to: String?,
    val variables: Map<String, String> = emptyMap(),
) {
    fun toCommand(): AlimtalkSendCommand =
        AlimtalkSendCommand(
            templateCode = requireNotNull(templateCode),
            to = requireNotNull(to),
            variables = variables,
        )
}

data class AlimtalkSendResponse(
    val templateCode: AlimtalkTemplateCode,
    val to: String,
    val status: String,
    val messageId: String,
    val sentAt: OffsetDateTime,
)

fun AlimtalkSendResult.toResponse(): AlimtalkSendResponse =
    AlimtalkSendResponse(
        templateCode = templateCode,
        to = to,
        status = status,
        messageId = messageId,
        sentAt = sentAt,
    )
