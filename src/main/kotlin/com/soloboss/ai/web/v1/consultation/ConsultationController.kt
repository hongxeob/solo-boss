package com.soloboss.ai.web.v1.consultation

import com.soloboss.ai.application.consultation.ConsultationService
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
@RequestMapping("/api/v1/consultations")
class ConsultationController(
    private val consultationService: ConsultationService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateConsultationRequest,
    ): ConsultationResponse = consultationService.create(request.toCommand()).toResponse()

    @GetMapping("/{consultationId}")
    fun get(
        @PathVariable consultationId: UUID,
        @RequestParam ownerId: UUID,
    ): ConsultationResponse = consultationService.get(ownerId = ownerId, consultationId = consultationId).toResponse()

    @GetMapping
    fun list(
        @RequestParam ownerId: UUID,
        @RequestParam(required = false) customerId: UUID?,
        pageable: Pageable,
    ): Page<ConsultationResponse> =
        if (customerId != null) {
            consultationService.listByCustomer(ownerId = ownerId, customerId = customerId, pageable = pageable)
        } else {
            consultationService.list(ownerId = ownerId, pageable = pageable)
        }.map { it.toResponse() }

    @PatchMapping("/{consultationId}")
    fun update(
        @PathVariable consultationId: UUID,
        @RequestParam ownerId: UUID,
        @RequestBody request: UpdateConsultationRequest,
    ): ConsultationResponse =
        consultationService
            .update(ownerId = ownerId, consultationId = consultationId, command = request.toCommand())
            .toResponse()

    @DeleteMapping("/{consultationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable consultationId: UUID,
        @RequestParam ownerId: UUID,
    ) {
        consultationService.delete(ownerId = ownerId, consultationId = consultationId)
    }
}
