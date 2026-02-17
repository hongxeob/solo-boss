package com.soloboss.ai.domain.interaction

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "review_tasks")
class ReviewTask(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    val id: UUID? = null,
    @Column(name = "owner_id", nullable = false, updatable = false)
    val ownerId: UUID,
    /** 연관 IngestJob ID (FK → ingest_jobs, 1:1 관계) */
    @Column(name = "ingest_job_id", nullable = false, unique = true)
    val ingestJobId: UUID,
    /** AI가 추측한 고객 이름 */
    @Column(name = "customer_guess", length = 100)
    var customerGuess: String? = null,
    /** 신뢰도가 낮은 필드 목록 (JSONB) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "uncertain_fields", columnDefinition = "jsonb")
    var uncertainFields: List<String>? = null,
    /** 사용자에게 제안하는 고객 정보 페이로드 (JSONB) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "proposed_payload", columnDefinition = "jsonb")
    var proposedPayload: Map<String, Any?>? = null,
    /** 전체 신뢰도 점수 */
    @Column(name = "overall_confidence")
    var overallConfidence: Double? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReviewTaskStatus = ReviewTaskStatus.OPEN,
    /** 검토 만료 시각 */
    @Column(name = "expires_at")
    var expiresAt: OffsetDateTime? = null,
    /** 검토 완료 시각 */
    @Column(name = "resolved_at")
    var resolvedAt: OffsetDateTime? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    /** 상태 전이 (상태 머신 검증 포함) */
    fun transitionTo(target: ReviewTaskStatus) {
        status = status.transitionTo(target)
        updatedAt = OffsetDateTime.now()
        if (target == ReviewTaskStatus.RESOLVED) {
            resolvedAt = OffsetDateTime.now()
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}
