package com.soloboss.ai.domain.interaction

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "consultations")
class Consultation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    val id: UUID? = null,
    @Column(name = "owner_id", nullable = false, updatable = false)
    val ownerId: UUID,
    /** 연관 고객 ID (FK → customers) */
    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,
    /** 연관 IngestJob ID (FK → ingest_jobs, nullable — 수동 등록 시 null) */
    @Column(name = "ingest_job_id")
    val ingestJobId: UUID? = null,
    /** 상담 내용 요약 */
    @Column(nullable = false, columnDefinition = "TEXT")
    var summary: String,
    /** 원본 텍스트 (OCR 결과 등) */
    @Column(name = "raw_text", columnDefinition = "TEXT")
    var rawText: String? = null,
    /** 상담 일시 */
    @Column(name = "consultation_date", nullable = false)
    var consultationDate: OffsetDateTime = OffsetDateTime.now(),
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
