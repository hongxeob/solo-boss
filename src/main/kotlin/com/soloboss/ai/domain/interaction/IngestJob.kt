package com.soloboss.ai.domain.interaction

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "ingest_jobs")
class IngestJob(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    val id: UUID? = null,

    @Column(name = "owner_id", nullable = false, updatable = false)
    val ownerId: UUID,

    /** 카카오 웹훅 이벤트 ID */
    @Column(name = "event_id", length = 100)
    val eventId: String? = null,

    /** 카카오 메시지 ID */
    @Column(name = "message_id", length = 100)
    val messageId: String? = null,

    /** 카카오 채널 ID */
    @Column(name = "channel_id", length = 100)
    val channelId: String? = null,

    /** 카카오톡 사용자 식별 키 */
    @Column(name = "kakao_user_key", length = 100)
    val kakaoUserKey: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    val sourceType: SourceType,

    /** 원본 이미지/음성 파일 URL (S3 등) */
    @Column(name = "source_url", length = 2048)
    var sourceUrl: String? = null,

    /** 멱등 처리용 고유 키 */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    val idempotencyKey: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: IngestJobStatus = IngestJobStatus.RECEIVED,

    /** 전체 신뢰도 (SQL 쿼리용 별도 컬럼) */
    @Column(name = "overall_confidence")
    var overallConfidence: Double? = null,

    /** 필드별 신뢰도를 포함한 구조화 추출 결과 (JSONB) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extraction_result", columnDefinition = "jsonb")
    var extractionResult: ExtractionResult? = null,

    /** OCR 원시 텍스트 */
    @Column(name = "ocr_raw_text", columnDefinition = "TEXT")
    var ocrRawText: String? = null,

    /** 실패 시 오류 사유 */
    @Column(name = "error_reason", columnDefinition = "TEXT")
    var errorReason: String? = null,

    /** 웹훅 수신 시각 */
    @Column(name = "received_at", nullable = false, updatable = false)
    val receivedAt: OffsetDateTime = OffsetDateTime.now(),

    /** 처리 완료 시각 */
    @Column(name = "processed_at")
    var processedAt: OffsetDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
) {
    /** 상태 전이 (상태 머신 검증 포함) */
    fun transitionTo(target: IngestJobStatus) {
        status = status.transitionTo(target)
        updatedAt = OffsetDateTime.now()
        if (target in setOf(IngestJobStatus.AUTO_SAVED, IngestJobStatus.NEEDS_REVIEW, IngestJobStatus.FAILED)) {
            processedAt = OffsetDateTime.now()
        }
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}
