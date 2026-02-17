package com.soloboss.ai.domain.interaction

/**
 * IngestJob 상태 머신.
 *
 * 전이 규칙:
 * ```
 * RECEIVED → OCR_DONE → STRUCTURED → AUTO_SAVED (confidence >= 0.85)
 *                                   → NEEDS_REVIEW (confidence < 0.85)
 *                                   → FAILED
 * RECEIVED → FAILED (어느 단계에서든 실패 가능)
 * OCR_DONE → FAILED
 * NEEDS_REVIEW → EXPIRED
 * ```
 */
enum class IngestJobStatus {
    /** 카카오 웹훅 수신 완료 */
    RECEIVED,

    /** OCR(비전) 처리 완료, 원시 텍스트 추출됨 */
    OCR_DONE,

    /** 구조화 추출 완료, 신뢰도 산출됨 */
    STRUCTURED,

    /** 신뢰도 >= 0.85, 자동 저장 완료 */
    AUTO_SAVED,

    /** 신뢰도 < 0.85, 사용자 검토 필요 */
    NEEDS_REVIEW,

    /** 처리 중 오류 발생 */
    FAILED,

    /** 검토 미완료로 만료됨 */
    EXPIRED;

    /** 현재 상태에서 [target]으로 전이 가능한지 확인 */
    fun canTransitionTo(target: IngestJobStatus): Boolean =
        target in allowedTransitions()

    /** 현재 상태에서 [target]으로 전이. 불가능하면 예외 발생 */
    fun transitionTo(target: IngestJobStatus): IngestJobStatus {
        require(canTransitionTo(target)) {
            "IngestJob 상태 전이 불가: $this → $target"
        }
        return target
    }

    private fun allowedTransitions(): Set<IngestJobStatus> = when (this) {
        RECEIVED -> setOf(OCR_DONE, FAILED)
        OCR_DONE -> setOf(STRUCTURED, FAILED)
        STRUCTURED -> setOf(AUTO_SAVED, NEEDS_REVIEW, FAILED)
        NEEDS_REVIEW -> setOf(EXPIRED)
        AUTO_SAVED, FAILED, EXPIRED -> emptySet()
    }
}
