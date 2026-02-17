package com.soloboss.ai.application.customer

import com.soloboss.ai.domain.customer.Customer
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class CustomerService(
    private val customerRepository: CustomerRepository,
) {
    @Transactional
    fun create(command: CreateCustomerCommand): Customer =
        customerRepository.save(
            Customer(
                ownerId = command.ownerId,
                name = command.name,
                phone = command.phone,
                email = command.email,
                kakaoUserKey = command.kakaoUserKey,
                projectType = command.projectType,
                estimatedBudget = command.estimatedBudget,
                inquirySummary = command.inquirySummary,
                notes = command.notes,
                source = command.source,
            ),
        )

    fun get(
        ownerId: UUID,
        customerId: UUID,
    ): Customer {
        val customer =
            customerRepository.findById(customerId).orElseThrow {
                EntityNotFoundException("고객을 찾을 수 없습니다. customerId=$customerId")
            }
        if (customer.ownerId != ownerId) {
            throw EntityNotFoundException("고객을 찾을 수 없습니다. customerId=$customerId")
        }
        return customer
    }

    fun list(
        ownerId: UUID,
        query: String?,
        pageable: Pageable,
    ): Page<Customer> =
        if (query.isNullOrBlank()) {
            customerRepository.findByOwnerId(ownerId, pageable)
        } else {
            customerRepository.findByOwnerIdAndNameContaining(ownerId, query.trim(), pageable)
        }

    @Transactional
    fun update(
        ownerId: UUID,
        customerId: UUID,
        command: UpdateCustomerCommand,
    ): Customer {
        val customer = get(ownerId = ownerId, customerId = customerId)

        command.name?.let { customer.name = it }
        customer.phone = command.phone
        customer.email = command.email
        customer.projectType = command.projectType
        customer.estimatedBudget = command.estimatedBudget
        customer.inquirySummary = command.inquirySummary
        customer.notes = command.notes

        return customer
    }

    @Transactional
    fun delete(
        ownerId: UUID,
        customerId: UUID,
    ) {
        val customer = get(ownerId = ownerId, customerId = customerId)
        customerRepository.delete(customer)
    }
}
