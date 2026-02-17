package com.soloboss.ai.application.consultation

import java.time.OffsetDateTime
import java.util.UUID

data class CreateConsultationCommand(
    val ownerId: UUID,
    val customerId: UUID,
    val summary: String,
    val rawText: String? = null,
    val consultationDate: OffsetDateTime? = null,
)

data class UpdateConsultationCommand(
    val summary: String? = null,
    val rawText: String? = null,
    val consultationDate: OffsetDateTime? = null,
)
