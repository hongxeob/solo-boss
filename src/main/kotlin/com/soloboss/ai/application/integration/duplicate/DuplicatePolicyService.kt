package com.soloboss.ai.application.integration.duplicate

import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class DuplicateType {
    UNIQUE,
    EXACT_DUPLICATE,
    NEAR_DUPLICATE,
}

enum class DuplicateTimelineType {
    MERGED,
    UNDO,
}

data class DuplicateResolution(
    val type: DuplicateType,
    val canonicalIdempotencyKey: String,
    val undoToken: String? = null,
)

data class UndoResult(
    val undone: Boolean,
    val canonicalIdempotencyKey: String? = null,
)

data class DuplicateTimelineEvent(
    val ownerId: UUID,
    val type: DuplicateTimelineType,
    val duplicateIdempotencyKey: String,
    val canonicalIdempotencyKey: String,
    val happenedAt: OffsetDateTime,
)

@Service
class DuplicatePolicyService {
    private val seenKeys = ConcurrentHashMap<String, OffsetDateTime>()
    private val recentByOwnerUser = ConcurrentHashMap<String, RecentCandidate>()
    private val mergeRecordsByToken = ConcurrentHashMap<String, MergeRecord>()
    private val mergeOverrideByKey = ConcurrentHashMap<String, String>()
    private val timelineByOwner = ConcurrentHashMap<UUID, MutableList<DuplicateTimelineEvent>>()

    fun resolve(
        ownerId: UUID,
        kakaoUserKey: String,
        idempotencyKey: String,
        now: OffsetDateTime = OffsetDateTime.now(),
    ): DuplicateResolution {
        val override = mergeOverrideByKey[idempotencyKey]
        if (override != null) {
            return DuplicateResolution(
                type = DuplicateType.NEAR_DUPLICATE,
                canonicalIdempotencyKey = override,
            )
        }

        if (seenKeys.putIfAbsent(idempotencyKey, now) != null) {
            return DuplicateResolution(
                type = DuplicateType.EXACT_DUPLICATE,
                canonicalIdempotencyKey = idempotencyKey,
            )
        }

        val ownerUserKey = "$ownerId:$kakaoUserKey"
        val recent = recentByOwnerUser[ownerUserKey]
        if (recent != null &&
            recent.idempotencyKey != idempotencyKey &&
            recent.receivedAt.plusMinutes(NEAR_DUPLICATE_WINDOW_MINUTES) >= now
        ) {
            val undoToken = UUID.randomUUID().toString()
            mergeRecordsByToken[undoToken] =
                MergeRecord(
                    ownerId = ownerId,
                    duplicateIdempotencyKey = idempotencyKey,
                    canonicalIdempotencyKey = recent.idempotencyKey,
                    expiresAt = now.plusHours(UNDO_HOURS),
                    active = true,
                )
            mergeOverrideByKey[idempotencyKey] = recent.idempotencyKey
            appendTimeline(
                ownerId = ownerId,
                type = DuplicateTimelineType.MERGED,
                duplicateIdempotencyKey = idempotencyKey,
                canonicalIdempotencyKey = recent.idempotencyKey,
                now = now,
            )
            return DuplicateResolution(
                type = DuplicateType.NEAR_DUPLICATE,
                canonicalIdempotencyKey = recent.idempotencyKey,
                undoToken = undoToken,
            )
        }

        recentByOwnerUser[ownerUserKey] = RecentCandidate(idempotencyKey = idempotencyKey, receivedAt = now)
        return DuplicateResolution(type = DuplicateType.UNIQUE, canonicalIdempotencyKey = idempotencyKey)
    }

    fun undo(
        ownerId: UUID,
        undoToken: String,
        now: OffsetDateTime = OffsetDateTime.now(),
    ): UndoResult {
        val record = mergeRecordsByToken[undoToken] ?: return UndoResult(false)
        if (!record.active || record.ownerId != ownerId || record.expiresAt < now) {
            return UndoResult(false)
        }

        mergeOverrideByKey.remove(record.duplicateIdempotencyKey)
        seenKeys.remove(record.duplicateIdempotencyKey)
        mergeRecordsByToken[undoToken] = record.copy(active = false)
        appendTimeline(
            ownerId = ownerId,
            type = DuplicateTimelineType.UNDO,
            duplicateIdempotencyKey = record.duplicateIdempotencyKey,
            canonicalIdempotencyKey = record.canonicalIdempotencyKey,
            now = now,
        )
        return UndoResult(true, canonicalIdempotencyKey = record.duplicateIdempotencyKey)
    }

    fun timeline(ownerId: UUID): List<DuplicateTimelineEvent> = timelineByOwner[ownerId].orEmpty().toList()

    private fun appendTimeline(
        ownerId: UUID,
        type: DuplicateTimelineType,
        duplicateIdempotencyKey: String,
        canonicalIdempotencyKey: String,
        now: OffsetDateTime,
    ) {
        val list = timelineByOwner.computeIfAbsent(ownerId) { mutableListOf() }
        list +=
            DuplicateTimelineEvent(
                ownerId = ownerId,
                type = type,
                duplicateIdempotencyKey = duplicateIdempotencyKey,
                canonicalIdempotencyKey = canonicalIdempotencyKey,
                happenedAt = now,
            )
    }

    data class RecentCandidate(
        val idempotencyKey: String,
        val receivedAt: OffsetDateTime,
    )

    data class MergeRecord(
        val ownerId: UUID,
        val duplicateIdempotencyKey: String,
        val canonicalIdempotencyKey: String,
        val expiresAt: OffsetDateTime,
        val active: Boolean,
    )

    companion object {
        private const val NEAR_DUPLICATE_WINDOW_MINUTES = 10L
        private const val UNDO_HOURS = 24L
    }
}
