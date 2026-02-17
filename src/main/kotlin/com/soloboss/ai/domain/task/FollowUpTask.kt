package com.soloboss.ai.domain.task

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "follow_up_tasks")
class FollowUpTask(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    val id: UUID? = null,
    @Column(name = "owner_id", nullable = false, updatable = false)
    val ownerId: UUID,
    /** 연관 고객 ID (FK → customers) */
    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,
    /** 연관 상담 기록 ID (FK → consultations, nullable) */
    @Column(name = "consultation_id")
    val consultationId: UUID? = null,
    /** 팔로업 목적/주제 */
    @Column(nullable = false, length = 500)
    var objective: String,
    /** AI가 생성한 초안 메시지 */
    @Column(name = "draft_content", columnDefinition = "TEXT")
    var draftContent: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: FollowUpTaskStatus = FollowUpTaskStatus.SCHEDULED,
    /** 팔로업 예정 시각 */
    @Column(name = "scheduled_at")
    var scheduledAt: OffsetDateTime? = null,
    /** 실제 발송 시각 */
    @Column(name = "sent_at")
    var sentAt: OffsetDateTime? = null,
    /** 스누즈 해제 시각 */
    @Column(name = "snoozed_until")
    var snoozedUntil: OffsetDateTime? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    /** 상태 전이 (상태 머신 검증 포함) */
    fun transitionTo(target: FollowUpTaskStatus) {
        status = status.transitionTo(target)
        updatedAt = OffsetDateTime.now()
        if (target == FollowUpTaskStatus.SENT) {
            sentAt = OffsetDateTime.now()
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}
