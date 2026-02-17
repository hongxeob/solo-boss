package com.soloboss.ai.infrastructure.ai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import java.time.OffsetDateTime
import java.util.UUID

class ConsultationEmbeddingServiceTest {
    private val vectorStore = Mockito.mock(VectorStore::class.java)
    private val service = ConsultationEmbeddingService(vectorStore)

    private val consultationId = UUID.randomUUID()
    private val ownerId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()
    private val consultationDate = OffsetDateTime.now()

    @Test
    fun `embed deletes existing and adds new document with correct metadata`() {
        service.embed(
            consultationId = consultationId,
            ownerId = ownerId,
            customerId = customerId,
            summary = "고객이 웹사이트 리디자인을 문의함",
            rawText = "안녕하세요, 웹사이트 리디자인 가능한가요?",
            consultationDate = consultationDate,
        )

        Mockito.verify(vectorStore).delete(listOf(consultationId.toString()))

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<Document>>
        Mockito.verify(vectorStore).add(captor.capture())

        val documents = captor.value
        assertEquals(1, documents.size)

        val doc = documents[0]
        assertEquals(consultationId.toString(), doc.id)
        assertTrue(doc.text!!.contains("고객이 웹사이트 리디자인을 문의함"))
        assertTrue(doc.text!!.contains("안녕하세요, 웹사이트 리디자인 가능한가요?"))
        assertEquals(ownerId.toString(), doc.metadata["ownerId"])
        assertEquals(customerId.toString(), doc.metadata["customerId"])
        assertEquals(consultationId.toString(), doc.metadata["consultationId"])
        assertEquals(consultationDate.toString(), doc.metadata["consultationDate"])
    }

    @Test
    fun `embed uses summary only when rawText is null`() {
        service.embed(
            consultationId = consultationId,
            ownerId = ownerId,
            customerId = customerId,
            summary = "간단 상담",
            rawText = null,
            consultationDate = consultationDate,
        )

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<Document>>
        Mockito.verify(vectorStore).add(captor.capture())

        assertEquals("간단 상담", captor.value[0].text)
    }

    @Test
    fun `remove calls vectorStore delete with consultationId`() {
        service.remove(consultationId)

        Mockito.verify(vectorStore).delete(listOf(consultationId.toString()))
    }

    @Test
    fun `composeEmbeddingText combines summary and rawText`() {
        val result = service.composeEmbeddingText("요약", "원본 텍스트")

        assertEquals("요약\n\n원본 텍스트", result)
    }

    @Test
    fun `composeEmbeddingText returns summary only when rawText is blank`() {
        assertEquals("요약", service.composeEmbeddingText("요약", ""))
        assertEquals("요약", service.composeEmbeddingText("요약", "   "))
        assertEquals("요약", service.composeEmbeddingText("요약", null))
    }

    @Test
    fun `composeEmbeddingText truncates at 6000 characters`() {
        val longText = "가".repeat(7000)

        val result = service.composeEmbeddingText(longText, null)

        assertEquals(6000, result.length)
    }
}
