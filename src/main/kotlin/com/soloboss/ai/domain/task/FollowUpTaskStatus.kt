package com.soloboss.ai.domain.task

/**
 * FollowUpTask 상태 머신.
 *
 * 전이 규칙:
 * ```
 * SCHEDULED → DRAFT_READY → SENT
 *                         → EDITING → SENT
 *                         → SNOOZED → DRAFT_READY
 * SCHEDULED → CANCELED
 * DRAFT_READY → CANCELED
 * EDITING → CANCELED
 * SNOOZED → CANCELED
 * ```
 */
enum class FollowUpTaskStatus {
    /** 팔로업 예정, 아직 초안 미생성 */
    SCHEDULED,

    /** AI 초안 생성 완료, 발송 대기 */
    DRAFT_READY,

    /** 사용자가 초안 수정 중 */
    EDITING,

    /** 팔로업 메시지 발송 완료 */
    SENT,

    /** 나중으로 미룸 */
    SNOOZED,

    /** 취소됨 */
    CANCELED,

    ;

    fun canTransitionTo(target: FollowUpTaskStatus): Boolean = target in allowedTransitions()

    fun transitionTo(target: FollowUpTaskStatus): FollowUpTaskStatus {
        require(canTransitionTo(target)) {
            "FollowUpTask 상태 전이 불가: $this → $target"
        }
        return target
    }

    private fun allowedTransitions(): Set<FollowUpTaskStatus> =
        when (this) {
            SCHEDULED -> setOf(DRAFT_READY, CANCELED)
            DRAFT_READY -> setOf(SENT, EDITING, SNOOZED, CANCELED)
            EDITING -> setOf(SENT, CANCELED)
            SNOOZED -> setOf(DRAFT_READY, CANCELED)
            SENT, CANCELED -> emptySet()
        }
}
