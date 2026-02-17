package com.soloboss.ai.web.v1.ocr

import com.soloboss.ai.application.ocr.OcrExtractionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/ocr")
class OcrController(
    private val ocrExtractionService: OcrExtractionService,
) {
    @PostMapping("/extract")
    @ResponseStatus(HttpStatus.CREATED)
    fun extract(
        @Valid @RequestBody request: OcrExtractRequest,
    ): OcrExtractResponse = ocrExtractionService.extract(request.toCommand()).toResponse()

    @GetMapping("/jobs/{jobId}")
    fun getJob(
        @PathVariable jobId: UUID,
        @RequestParam ownerId: UUID,
    ): OcrExtractResponse = ocrExtractionService.get(ownerId = ownerId, jobId = jobId).toResponse()
}
