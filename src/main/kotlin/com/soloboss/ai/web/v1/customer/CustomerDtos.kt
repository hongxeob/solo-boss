package com.soloboss.ai.web.v1.customer

import com.soloboss.ai.application.customer.CreateCustomerCommand
import com.soloboss.ai.application.customer.UpdateCustomerCommand
import com.soloboss.ai.domain.customer.Customer
import com.soloboss.ai.domain.customer.CustomerSource
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

data class CreateCustomerRequest(
    @field:NotNull
    val ownerId: UUID?,
    @field:NotBlank
    val name: String?,
    val phone: String? = null,
    val email: String? = null,
    val kakaoUserKey: String? = null,
    val projectType: String? = null,
    val estimatedBudget: String? = null,
    val inquirySummary: String? = null,
    val notes: String? = null,
    val source: CustomerSource = CustomerSource.MANUAL,
) {
    fun toCommand(): CreateCustomerCommand =
        CreateCustomerCommand(
            ownerId = requireNotNull(ownerId),
            name = requireNotNull(name).trim(),
            phone = phone,
            email = email,
            kakaoUserKey = kakaoUserKey,
            projectType = projectType,
            estimatedBudget = estimatedBudget,
            inquirySummary = inquirySummary,
            notes = notes,
            source = source,
        )
}

data class UpdateCustomerRequest(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val projectType: String? = null,
    val estimatedBudget: String? = null,
    val inquirySummary: String? = null,
    val notes: String? = null,
) {
    fun toCommand(): UpdateCustomerCommand =
        UpdateCustomerCommand(
            name = name,
            phone = phone,
            email = email,
            projectType = projectType,
            estimatedBudget = estimatedBudget,
            inquirySummary = inquirySummary,
            notes = notes,
        )
}

data class CustomerResponse(
    val id: UUID,
    val ownerId: UUID,
    val name: String,
    val phone: String?,
    val email: String?,
    val kakaoUserKey: String?,
    val projectType: String?,
    val estimatedBudget: String?,
    val inquirySummary: String?,
    val notes: String?,
    val source: CustomerSource,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

fun Customer.toResponse(): CustomerResponse =
    CustomerResponse(
        id = requireNotNull(id),
        ownerId = ownerId,
        name = name,
        phone = phone,
        email = email,
        kakaoUserKey = kakaoUserKey,
        projectType = projectType,
        estimatedBudget = estimatedBudget,
        inquirySummary = inquirySummary,
        notes = notes,
        source = source,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
