package com.soloboss.ai.web.v1.customer

import com.soloboss.ai.application.customer.CustomerService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/customers")
class CustomerController(
    private val customerService: CustomerService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateCustomerRequest,
    ): CustomerResponse = customerService.create(request.toCommand()).toResponse()

    @GetMapping("/{customerId}")
    fun get(
        @PathVariable customerId: UUID,
        @RequestParam ownerId: UUID,
    ): CustomerResponse = customerService.get(ownerId = ownerId, customerId = customerId).toResponse()

    @GetMapping
    fun list(
        @RequestParam ownerId: UUID,
        @RequestParam(required = false) query: String?,
        pageable: Pageable,
    ): Page<CustomerResponse> =
        customerService
            .list(ownerId = ownerId, query = query, pageable = pageable)
            .map { it.toResponse() }

    @PatchMapping("/{customerId}")
    fun update(
        @PathVariable customerId: UUID,
        @RequestParam ownerId: UUID,
        @RequestBody request: UpdateCustomerRequest,
    ): CustomerResponse =
        customerService
            .update(ownerId = ownerId, customerId = customerId, command = request.toCommand())
            .toResponse()

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable customerId: UUID,
        @RequestParam ownerId: UUID,
    ) {
        customerService.delete(ownerId = ownerId, customerId = customerId)
    }
}
