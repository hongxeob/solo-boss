package com.soloboss.ai.infrastructure.ai

import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Consultation 텍스트를 벡터 저장소에 임베딩하는 인프라 컴포넌트.
 * VectorStore를 감싸서 Document 생성/삭제를 담당한다.
 */
@Component
class ConsultationEmbeddingService(
    private val vectorStore: VectorStore,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_TEXT_LENGTH = 6000
    }

    /**
     * 상담 텍스트를 임베딩하여 벡터 저장소에 저장한다.
     * 동일 consultationId의 기존 Document가 있으면 삭제 후 재생성한다.
     */
    fun embed(
        consultationId: UUID,
        ownerId: UUID,
        customerId: UUID,
        summary: String,
        rawText: String?,
        consultationDate: OffsetDateTime,
    ) {
        remove(consultationId)

        val text = composeEmbeddingText(summary, rawText)
        val metadata =
            mapOf(
                "ownerId" to ownerId.toString(),
                "customerId" to customerId.toString(),
                "consultationId" to consultationId.toString(),
                "consultationDate" to consultationDate.toString(),
            )
        val document = Document(consultationId.toString(), text, metadata)

        vectorStore.add(listOf(document))
        logger.debug("임베딩 저장 완료: consultationId={}", consultationId)
    }

    /**
     * 벡터 저장소에서 해당 상담의 Document를 삭제한다.
     */
    fun remove(consultationId: UUID) {
        vectorStore.delete(listOf(consultationId.toString()))
        logger.debug("임베딩 삭제: consultationId={}", consultationId)
    }

    /**
     * summary와 rawText를 결합하여 임베딩 텍스트를 생성한다.
     * 최대 6000자로 제한 (text-embedding-3-small 토큰 제한 안전 마진).
     */
    fun composeEmbeddingText(
        summary: String,
        rawText: String?,
    ): String {
        val combined =
            if (rawText.isNullOrBlank()) {
                summary
            } else {
                "$summary\n\n$rawText"
            }
        return if (combined.length > MAX_TEXT_LENGTH) {
            combined.substring(0, MAX_TEXT_LENGTH)
        } else {
            combined
        }
    }
}
