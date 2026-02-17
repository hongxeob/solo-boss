package com.soloboss.ai.application.memory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.ByteArrayResource
import java.util.UUID

class RelationshipMemoryServiceTest {
    private val vectorStore = Mockito.mock(VectorStore::class.java)
    private val chatClientBuilder = Mockito.mock(ChatClient.Builder::class.java)
    private val summaryPromptResource = ByteArrayResource("테스트 프롬프트\n{context}".toByteArray())
    private val service = RelationshipMemoryService(vectorStore, chatClientBuilder, summaryPromptResource)

    private val ownerId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()

    @Test
    fun `search returns results with correct metadata mapping`() {
        val consultationId = UUID.randomUUID()
        val doc = documentWith(consultationId, "상담 내용 텍스트", "2026-01-15T10:00:00+09:00", 0.92)
        Mockito.`when`(vectorStore.similaritySearch(any(SearchRequest::class.java))).thenReturn(listOf(doc))

        val results =
            service.search(
                MemorySearchCommand(
                    ownerId = ownerId,
                    customerId = customerId,
                    query = "웹사이트",
                ),
            )

        assertEquals(1, results.size)
        assertEquals(consultationId, results[0].consultationId)
        assertEquals(customerId, results[0].customerId)
        assertEquals("상담 내용 텍스트", results[0].content)
        assertEquals(0.92, results[0].score)
        assertEquals("2026-01-15T10:00:00+09:00", results[0].consultationDate)
    }

    @Test
    fun `search returns empty list when no documents found`() {
        Mockito.`when`(vectorStore.similaritySearch(any(SearchRequest::class.java))).thenReturn(emptyList())

        val results =
            service.search(
                MemorySearchCommand(
                    ownerId = ownerId,
                    query = "존재하지 않는 내용",
                ),
            )

        assertTrue(results.isEmpty())
    }

    @Test
    fun `search skips documents with missing metadata`() {
        val incompleteDoc =
            Document
                .builder()
                .id("incomplete")
                .text("내용")
                .metadata("ownerId", ownerId.toString())
                .build()
        Mockito.`when`(vectorStore.similaritySearch(any(SearchRequest::class.java))).thenReturn(listOf(incompleteDoc))

        val results =
            service.search(
                MemorySearchCommand(ownerId = ownerId, query = "검색어"),
            )

        assertTrue(results.isEmpty())
    }

    @Test
    fun `search handles null return from vectorStore`() {
        Mockito.`when`(vectorStore.similaritySearch(any(SearchRequest::class.java))).thenReturn(null)

        val results =
            service.search(
                MemorySearchCommand(ownerId = ownerId, query = "검색어"),
            )

        assertTrue(results.isEmpty())
    }

    @Test
    fun `summarize returns default message when no results found`() {
        Mockito.`when`(vectorStore.similaritySearch(any(SearchRequest::class.java))).thenReturn(emptyList())

        val result =
            service.summarize(
                MemorySummarizeCommand(
                    ownerId = ownerId,
                    customerId = customerId,
                ),
            )

        assertEquals("관련 상담 기록이 없습니다.", result.summary)
        assertEquals(0, result.sourceCount)
        assertTrue(result.sources.isEmpty())
    }

    @Test
    fun `summarize calls ChatClient with search results as context`() {
        val consultationId = UUID.randomUUID()
        val doc = documentWith(consultationId, "견적 문의 내용", "2026-02-01", 0.88)
        Mockito.`when`(vectorStore.similaritySearch(any(SearchRequest::class.java))).thenReturn(listOf(doc))

        val chatClient = Mockito.mock(ChatClient::class.java)
        val requestSpec = Mockito.mock(ChatClientRequestSpec::class.java)
        val callResponseSpec = Mockito.mock(CallResponseSpec::class.java)

        Mockito.`when`(chatClientBuilder.build()).thenReturn(chatClient)
        Mockito.`when`(chatClient.prompt()).thenReturn(requestSpec)
        Mockito.`when`(requestSpec.user(any(String::class.java))).thenReturn(requestSpec)
        Mockito.`when`(requestSpec.call()).thenReturn(callResponseSpec)
        Mockito.`when`(callResponseSpec.content()).thenReturn("고객과 견적 관련 상담을 진행했습니다.")

        val result =
            service.summarize(
                MemorySummarizeCommand(
                    ownerId = ownerId,
                    customerId = customerId,
                    query = "견적 관련",
                ),
            )

        assertEquals("고객과 견적 관련 상담을 진행했습니다.", result.summary)
        assertEquals(1, result.sourceCount)
        assertEquals(consultationId, result.sources[0].consultationId)
    }

    @Test
    fun `summarize returns error message when ChatClient fails`() {
        val consultationId = UUID.randomUUID()
        val doc = documentWith(consultationId, "내용", null, 0.8)
        Mockito.`when`(vectorStore.similaritySearch(any(SearchRequest::class.java))).thenReturn(listOf(doc))
        Mockito.`when`(chatClientBuilder.build()).thenThrow(RuntimeException("API 장애"))

        val result =
            service.summarize(
                MemorySummarizeCommand(
                    ownerId = ownerId,
                    customerId = customerId,
                ),
            )

        assertEquals("요약 생성 중 오류가 발생했습니다.", result.summary)
        assertEquals(1, result.sourceCount)
    }

    private fun documentWith(
        consultationId: UUID,
        text: String,
        consultationDate: String?,
        score: Double,
    ): Document {
        val metadata =
            mutableMapOf<String, Any>(
                "ownerId" to ownerId.toString(),
                "customerId" to customerId.toString(),
                "consultationId" to consultationId.toString(),
            )
        consultationDate?.let { metadata["consultationDate"] = it }

        return Document
            .builder()
            .id(consultationId.toString())
            .text(text)
            .metadata(metadata)
            .score(score)
            .build()
    }
}
