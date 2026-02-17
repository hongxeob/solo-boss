package com.soloboss.ai.application.customer

import com.soloboss.ai.domain.customer.CustomerSource
import java.util.UUID

data class CreateCustomerCommand(
    val ownerId: UUID,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val kakaoUserKey: String? = null,
    val projectType: String? = null,
    val estimatedBudget: String? = null,
    val inquirySummary: String? = null,
    val notes: String? = null,
    val source: CustomerSource = CustomerSource.MANUAL,
)

data class UpdateCustomerCommand(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val projectType: String? = null,
    val estimatedBudget: String? = null,
    val inquirySummary: String? = null,
    val notes: String? = null,
)
