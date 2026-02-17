package com.soloboss.ai.web.v1.memory

import com.soloboss.ai.application.memory.RelationshipMemoryService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/memory")
class MemoryController(
    private val memoryService: RelationshipMemoryService,
) {
    @PostMapping("/search")
    fun search(
        @Valid @RequestBody request: MemorySearchRequest,
    ): MemorySearchResponse {
        val results = memoryService.search(request.toCommand())
        return MemorySearchResponse(
            results = results.map { it.toResponse() },
            totalCount = results.size,
        )
    }

    @PostMapping("/summarize")
    fun summarize(
        @Valid @RequestBody request: MemorySummarizeRequest,
    ): MemorySummarizeResponse {
        val result = memoryService.summarize(request.toCommand())
        return MemorySummarizeResponse(
            summary = result.summary,
            sourceCount = result.sourceCount,
            sources = result.sources.map { it.toResponse() },
        )
    }
}
