package com.soloboss.ai.web.v1.consultation

import com.soloboss.ai.application.consultation.CreateConsultationCommand
import com.soloboss.ai.application.consultation.UpdateConsultationCommand
import com.soloboss.ai.domain.interaction.Consultation
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

data class CreateConsultationRequest(
    @field:NotNull
    val ownerId: UUID?,
    @field:NotNull
    val customerId: UUID?,
    @field:NotBlank
    val summary: String?,
    val rawText: String? = null,
    val consultationDate: OffsetDateTime? = null,
) {
    fun toCommand(): CreateConsultationCommand =
        CreateConsultationCommand(
            ownerId = requireNotNull(ownerId),
            customerId = requireNotNull(customerId),
            summary = requireNotNull(summary).trim(),
            rawText = rawText,
            consultationDate = consultationDate,
        )
}

data class UpdateConsultationRequest(
    val summary: String? = null,
    val rawText: String? = null,
    val consultationDate: OffsetDateTime? = null,
) {
    fun toCommand(): UpdateConsultationCommand =
        UpdateConsultationCommand(
            summary = summary,
            rawText = rawText,
            consultationDate = consultationDate,
        )
}

data class ConsultationResponse(
    val id: UUID,
    val ownerId: UUID,
    val customerId: UUID,
    val ingestJobId: UUID?,
    val summary: String,
    val rawText: String?,
    val consultationDate: OffsetDateTime,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

fun Consultation.toResponse(): ConsultationResponse =
    ConsultationResponse(
        id = requireNotNull(id),
        ownerId = ownerId,
        customerId = customerId,
        ingestJobId = ingestJobId,
        summary = summary,
        rawText = rawText,
        consultationDate = consultationDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
