package com.soloboss.ai.web.v1.memory

import com.soloboss.ai.application.memory.MemorySearchResult
import com.soloboss.ai.application.memory.RelationshipMemoryService
import com.soloboss.ai.application.memory.RelationshipSummary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.UUID

class MemoryControllerTest {
    private val memoryService = Mockito.mock(RelationshipMemoryService::class.java)
    private val controller = MemoryController(memoryService)

    private val ownerId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()
    private val consultationId = UUID.randomUUID()

    @Test
    fun `search returns results with totalCount`() {
        val searchResult =
            MemorySearchResult(
                consultationId = consultationId,
                customerId = customerId,
                content = "상담 내용",
                score = 0.95,
                consultationDate = "2026-02-01",
            )
        Mockito.`when`(memoryService.search(anyObj())).thenReturn(listOf(searchResult))

        val response =
            controller.search(
                MemorySearchRequest(
                    ownerId = ownerId,
                    customerId = customerId,
                    query = "웹사이트",
                ),
            )

        assertEquals(1, response.totalCount)
        assertEquals(1, response.results.size)
        assertEquals(consultationId, response.results[0].consultationId)
        assertEquals(0.95, response.results[0].score)
    }

    @Test
    fun `search returns empty response when no results`() {
        Mockito.`when`(memoryService.search(anyObj())).thenReturn(emptyList())

        val response =
            controller.search(
                MemorySearchRequest(
                    ownerId = ownerId,
                    query = "없는 내용",
                ),
            )

        assertEquals(0, response.totalCount)
        assertEquals(0, response.results.size)
    }

    @Test
    fun `summarize returns summary with sources`() {
        val source =
            MemorySearchResult(
                consultationId = consultationId,
                customerId = customerId,
                content = "견적 문의",
                score = 0.88,
                consultationDate = "2026-01-15",
            )
        val summary =
            RelationshipSummary(
                summary = "고객과 견적 관련 상담을 진행했습니다.",
                sourceCount = 1,
                sources = listOf(source),
            )
        Mockito.`when`(memoryService.summarize(anyObj())).thenReturn(summary)

        val response =
            controller.summarize(
                MemorySummarizeRequest(
                    ownerId = ownerId,
                    customerId = customerId,
                ),
            )

        assertEquals("고객과 견적 관련 상담을 진행했습니다.", response.summary)
        assertEquals(1, response.sourceCount)
        assertEquals(consultationId, response.sources[0].consultationId)
    }

    @Test
    fun `summarize returns empty summary when no records`() {
        val emptySummary =
            RelationshipSummary(
                summary = "관련 상담 기록이 없습니다.",
                sourceCount = 0,
                sources = emptyList(),
            )
        Mockito.`when`(memoryService.summarize(anyObj())).thenReturn(emptySummary)

        val response =
            controller.summarize(
                MemorySummarizeRequest(
                    ownerId = ownerId,
                    customerId = customerId,
                ),
            )

        assertEquals("관련 상담 기록이 없습니다.", response.summary)
        assertEquals(0, response.sourceCount)
        assertEquals(0, response.sources.size)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyObj(): T = ArgumentMatchers.any<T>() as T
}
