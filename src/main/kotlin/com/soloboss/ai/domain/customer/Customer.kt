package com.soloboss.ai.domain.customer

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
@Table(name = "customers")
class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    val id: UUID? = null,
    /** 멀티테넌시용 소유자 ID */
    @Column(name = "owner_id", nullable = false, updatable = false)
    val ownerId: UUID,
    @Column(nullable = false, length = 100)
    var name: String,
    @Column(length = 20)
    var phone: String? = null,
    @Column(length = 255)
    var email: String? = null,
    /** 카카오톡 사용자 식별 키 */
    @Column(name = "kakao_user_key", length = 100)
    var kakaoUserKey: String? = null,
    /** 프로젝트/문의 유형 (예: "웹사이트 제작", "세무 상담") */
    @Column(name = "project_type", length = 100)
    var projectType: String? = null,
    /** 예상 예산 */
    @Column(name = "estimated_budget", length = 50)
    var estimatedBudget: String? = null,
    /** 문의 내용 요약 */
    @Column(name = "inquiry_summary", columnDefinition = "TEXT")
    var inquirySummary: String? = null,
    /** 자유 메모 */
    @Column(columnDefinition = "TEXT")
    var notes: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var source: CustomerSource = CustomerSource.MANUAL,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}
