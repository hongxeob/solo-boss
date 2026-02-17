package com.soloboss.ai.application.integration.duplicate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class DuplicatePolicyServiceTest {
    private val service = DuplicatePolicyService()

    @Test
    fun `near duplicate merges into previous key and creates undo token`() {
        val ownerId = UUID.randomUUID()
        val now = OffsetDateTime.now()

        val first = service.resolve(ownerId, "kakao_1", "ch:msg1", now)
        val second = service.resolve(ownerId, "kakao_1", "ch:msg2", now.plusMinutes(1))

        assertEquals(DuplicateType.UNIQUE, first.type)
        assertEquals(DuplicateType.NEAR_DUPLICATE, second.type)
        assertEquals("ch:msg1", second.canonicalIdempotencyKey)
        assertNotNull(second.undoToken)
        assertTrue(service.timeline(ownerId).any { it.type == DuplicateTimelineType.MERGED })
    }

    @Test
    fun `undo reactivates duplicate key as standalone`() {
        val ownerId = UUID.randomUUID()
        val now = OffsetDateTime.now()

        service.resolve(ownerId, "kakao_1", "ch:msg1", now)
        val merged = service.resolve(ownerId, "kakao_1", "ch:msg2", now.plusMinutes(1))
        val undoToken = requireNotNull(merged.undoToken)

        val undone = service.undo(ownerId, undoToken, now.plusMinutes(2))
        val afterUndo = service.resolve(ownerId, "kakao_1", "ch:msg2", now.plusMinutes(11))

        assertTrue(undone.undone)
        assertEquals(DuplicateType.UNIQUE, afterUndo.type)
        assertTrue(service.timeline(ownerId).any { it.type == DuplicateTimelineType.UNDO })
    }
}
