package com.soloboss.ai.application.notification

import java.time.OffsetDateTime
import java.util.UUID

enum class AlimtalkTemplateCode(
    val requiredVariables: Set<String>,
) {
    RECEIVED_ACK(setOf("eta_seconds", "source_type", "received_at", "job_status_link")),
    PROCESS_AUTO_DONE(setOf("customer_name", "summary_one_line", "followup_at", "customer_card_link", "followup_draft_link")),
    PROCESS_REVIEW_REQUIRED(setOf("review_count", "customer_guess", "uncertain_fields", "summary_one_line", "review_link")),
    FOLLOWUP_REMINDER(
        setOf(
            "customer_name",
            "followup_objective",
            "recommended_send_at",
            "draft_message",
            "send_now_link",
            "edit_draft_link",
            "snooze_1d_link",
        ),
    ),
    OCR_TEXT_ONLY(emptySet()),
    OCR_IMAGE_BLURRY(emptySet()),
    OCR_IMAGE_EXPOSURE(emptySet()),
    OCR_NOT_CONVERSATION(emptySet()),
    OCR_MULTI_IMAGE_ORDER(emptySet()),
}

data class AlimtalkSendCommand(
    val templateCode: AlimtalkTemplateCode,
    val to: String,
    val variables: Map<String, String> = emptyMap(),
)

data class AlimtalkSendResult(
    val templateCode: AlimtalkTemplateCode,
    val to: String,
    val status: String,
    val messageId: String = UUID.randomUUID().toString(),
    val sentAt: OffsetDateTime = OffsetDateTime.now(),
)

fun interface AlimtalkSenderGateway {
    fun send(command: AlimtalkSendCommand): AlimtalkSendResult
}
