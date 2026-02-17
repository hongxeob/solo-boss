package com.soloboss.ai.domain.interaction

/**
 * 필드별 신뢰도를 포함하는 추출 값.
 * JSONB로 직렬화되어 IngestJob.extractionResult에 저장된다.
 */
data class ConfidenceField<T>(
    val value: T?,
    val confidence: Double
)

/** 요약 텍스트(여러 줄)와 신뢰도 */
data class SummaryField(
    val lines: List<String>,
    val confidence: Double
)

/**
 * OCR + 구조화 추출의 전체 결과.
 *
 * IngestJob의 `extraction_result` JSONB 컬럼에 저장된다.
 * `overallConfidence`는 별도 DOUBLE 컬럼에도 중복 저장하여 SQL 쿼리를 지원한다.
 */
data class ExtractionResult(
    val name: ConfidenceField<String> = ConfidenceField(null, 0.0),
    val phone: ConfidenceField<String> = ConfidenceField(null, 0.0),
    val email: ConfidenceField<String> = ConfidenceField(null, 0.0),
    val projectType: ConfidenceField<String> = ConfidenceField(null, 0.0),
    val estimatedBudget: ConfidenceField<String> = ConfidenceField(null, 0.0),
    val inquirySummary: SummaryField = SummaryField(emptyList(), 0.0),
    val overallConfidence: Double = 0.0
)
