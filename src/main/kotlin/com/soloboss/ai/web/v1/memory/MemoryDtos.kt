package com.soloboss.ai.web.v1.memory

import com.soloboss.ai.application.memory.MemorySearchCommand
import com.soloboss.ai.application.memory.MemorySearchResult
import com.soloboss.ai.application.memory.MemorySummarizeCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class MemorySearchRequest(
    @field:NotNull
    val ownerId: UUID?,
    val customerId: UUID? = null,
    @field:NotBlank
    val query: String?,
    val topK: Int? = null,
    val similarityThreshold: Double? = null,
) {
    fun toCommand(): MemorySearchCommand =
        MemorySearchCommand(
            ownerId = requireNotNull(ownerId),
            customerId = customerId,
            query = requireNotNull(query).trim(),
            topK = topK ?: 5,
            similarityThreshold = similarityThreshold ?: 0.0,
        )
}

data class MemorySummarizeRequest(
    @field:NotNull
    val ownerId: UUID?,
    @field:NotNull
    val customerId: UUID?,
    val query: String? = null,
    val topK: Int? = null,
) {
    fun toCommand(): MemorySummarizeCommand =
        MemorySummarizeCommand(
            ownerId = requireNotNull(ownerId),
            customerId = requireNotNull(customerId),
            query = query?.trim(),
            topK = topK ?: 10,
        )
}

data class MemorySearchResultResponse(
    val consultationId: UUID,
    val customerId: UUID,
    val content: String,
    val score: Double,
    val consultationDate: String?,
)

data class MemorySearchResponse(
    val results: List<MemorySearchResultResponse>,
    val totalCount: Int,
)

data class MemorySummarizeResponse(
    val summary: String,
    val sourceCount: Int,
    val sources: List<MemorySearchResultResponse>,
)

fun MemorySearchResult.toResponse(): MemorySearchResultResponse =
    MemorySearchResultResponse(
        consultationId = consultationId,
        customerId = customerId,
        content = content,
        score = score,
        consultationDate = consultationDate,
    )
