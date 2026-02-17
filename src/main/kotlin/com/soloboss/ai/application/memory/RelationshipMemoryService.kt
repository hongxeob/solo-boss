package com.soloboss.ai.application.memory

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RelationshipMemoryService(
    private val vectorStore: VectorStore,
    private val chatClientBuilder: ChatClient.Builder,
    @Value("classpath:prompts/relationship-summary.st")
    private val summaryPromptResource: Resource,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 시맨틱 유사도 검색으로 관련 상담 이력을 찾는다.
     * ownerId 필수, customerId 선택적 필터링.
     */
    fun search(command: MemorySearchCommand): List<MemorySearchResult> {
        val filterBuilder = FilterExpressionBuilder()
        val filter =
            if (command.customerId != null) {
                filterBuilder
                    .and(
                        filterBuilder.eq("ownerId", command.ownerId.toString()),
                        filterBuilder.eq("customerId", command.customerId.toString()),
                    ).build()
            } else {
                filterBuilder
                    .eq("ownerId", command.ownerId.toString())
                    .build()
            }

        val searchRequest =
            SearchRequest
                .builder()
                .query(command.query)
                .topK(command.topK)
                .similarityThreshold(command.similarityThreshold)
                .filterExpression(filter)
                .build()

        val documents = vectorStore.similaritySearch(searchRequest).orEmpty()

        return documents.mapNotNull { doc ->
            val consultationId = doc.metadata["consultationId"]?.toString() ?: return@mapNotNull null
            val customerId = doc.metadata["customerId"]?.toString() ?: return@mapNotNull null

            MemorySearchResult(
                consultationId = UUID.fromString(consultationId),
                customerId = UUID.fromString(customerId),
                content = doc.text ?: return@mapNotNull null,
                score = doc.score ?: 0.0,
                consultationDate = doc.metadata["consultationDate"]?.toString(),
            )
        }
    }

    /**
     * 상담 이력을 검색하고 AI로 관계 요약을 생성한다.
     * 검색 결과가 없으면 기본 메시지를 반환한다.
     */
    fun summarize(command: MemorySummarizeCommand): RelationshipSummary {
        val searchResults =
            search(
                MemorySearchCommand(
                    ownerId = command.ownerId,
                    customerId = command.customerId,
                    query = command.query ?: "이 고객과의 전체 상담 이력을 요약해주세요",
                    topK = command.topK,
                ),
            )

        if (searchResults.isEmpty()) {
            return RelationshipSummary(
                summary = "관련 상담 기록이 없습니다.",
                sourceCount = 0,
                sources = emptyList(),
            )
        }

        val context =
            searchResults.joinToString("\n\n---\n\n") { result ->
                val dateInfo = result.consultationDate?.let { "[상담일: $it] " } ?: ""
                "$dateInfo${result.content}"
            }

        val promptTemplate = summaryPromptResource.getContentAsString(Charsets.UTF_8)
        val prompt = promptTemplate.replace("{context}", context)

        val summary =
            try {
                chatClientBuilder
                    .build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content() ?: "요약 생성에 실패했습니다."
            } catch (e: Exception) {
                logger.error("관계 요약 생성 실패: customerId={}", command.customerId, e)
                "요약 생성 중 오류가 발생했습니다."
            }

        return RelationshipSummary(
            summary = summary,
            sourceCount = searchResults.size,
            sources = searchResults,
        )
    }
}
