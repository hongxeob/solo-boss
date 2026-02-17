package com.soloboss.ai.infrastructure.ai

import com.soloboss.ai.application.ocr.OcrExtractionPayload
import com.soloboss.ai.application.ocr.OcrExtractor
import com.soloboss.ai.domain.interaction.ExtractionResult
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.context.annotation.Primary
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils

@Component
@Primary
class AnthropicOcrExtractor(
    private val chatClientBuilder: ChatClient.Builder,
    private val resourceLoader: ResourceLoader,
) : OcrExtractor {
    override fun extract(sourceUrl: String): OcrExtractionPayload {
        val converter = BeanOutputConverter(ExtractionResult::class.java)
        val format = converter.format
        val promptText = PROMPT_TEMPLATE.replace("{format}", format)
        val imageResource = resourceLoader.getResource(sourceUrl)

        val content =
            chatClientBuilder
                .build()
                .prompt()
                .user {
                    it.text(promptText)
                    it.media(MimeTypeUtils.IMAGE_JPEG, imageResource)
                }.call()
                .content() ?: throw IllegalStateException("OCR 응답이 비어 있습니다.")

        val extraction = requireNotNull(converter.convert(content)) { "OCR JSON 파싱에 실패했습니다." }
        val rawText = extraction.inquirySummary.lines.joinToString("\n")

        return OcrExtractionPayload(
            extractionResult = extraction,
            ocrRawText = rawText,
        )
    }

    companion object {
        private val PROMPT_TEMPLATE =
            """
            [Persona]
            당신은 전문직 프리랜서를 위한 비즈니스 비서 AI입니다. 이미지에서 상담 정보를 정밀하게 추출합니다.

            [Task]
            이미지에서 고객 정보를 추출하고 아래 JSON 포맷으로 반환하세요.

            [Constraints]
            1. 시각적으로 확인 가능한 정보만 사용하고 추측하지 마세요.
            2. 숫자 필드는 통화기호/콤마를 제거하세요.
            3. 날짜는 ISO-8601(YYYY-MM-DD) 형식으로 반환하세요.
            4. 인식 불가 값은 빈 문자열이 아닌 null로 반환하세요.
            5. 출력은 마크다운 코드블록 없이 순수 JSON만 반환하세요.

            [Output Format]
            {format}
            """.trimIndent()
    }
}
