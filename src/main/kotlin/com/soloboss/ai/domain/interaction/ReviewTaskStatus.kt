package com.soloboss.ai.domain.interaction

/**
 * ReviewTask 상태 머신.
 *
 * 전이 규칙:
 * ```
 * OPEN → IN_PROGRESS → RESOLVED
 * IN_PROGRESS → OPEN (임시 이탈)
 * OPEN → EXPIRED
 * IN_PROGRESS → EXPIRED
 * ```
 */
enum class ReviewTaskStatus {
    /** 검토 대기 중 */
    OPEN,

    /** 사용자가 검토 진행 중 */
    IN_PROGRESS,

    /** 검토 완료, 고객 정보 확정됨 */
    RESOLVED,

    /** 검토 기한 만료 */
    EXPIRED,

    ;

    fun canTransitionTo(target: ReviewTaskStatus): Boolean = target in allowedTransitions()

    fun transitionTo(target: ReviewTaskStatus): ReviewTaskStatus {
        require(canTransitionTo(target)) {
            "ReviewTask 상태 전이 불가: $this → $target"
        }
        return target
    }

    private fun allowedTransitions(): Set<ReviewTaskStatus> =
        when (this) {
            OPEN -> setOf(IN_PROGRESS, EXPIRED)
            IN_PROGRESS -> setOf(RESOLVED, OPEN, EXPIRED)
            RESOLVED, EXPIRED -> emptySet()
        }
}
