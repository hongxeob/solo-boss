package com.soloboss.ai.application.memory

import java.util.UUID

data class MemorySearchCommand(
    val ownerId: UUID,
    val customerId: UUID? = null,
    val query: String,
    val topK: Int = 5,
    val similarityThreshold: Double = 0.0,
)

data class MemorySearchResult(
    val consultationId: UUID,
    val customerId: UUID,
    val content: String,
    val score: Double,
    val consultationDate: String?,
)

data class MemorySummarizeCommand(
    val ownerId: UUID,
    val customerId: UUID,
    val query: String? = null,
    val topK: Int = 10,
)

data class RelationshipSummary(
    val summary: String,
    val sourceCount: Int,
    val sources: List<MemorySearchResult>,
)
