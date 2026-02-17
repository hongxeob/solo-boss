package com.soloboss.ai.web.v1.duplicate

import com.soloboss.ai.application.integration.duplicate.DuplicatePolicyService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class DuplicateUndoResponse(
    val undone: Boolean,
    val canonicalIdempotencyKey: String?,
)

@RestController
@RequestMapping("/api/v1/duplicates")
class DuplicateController(
    private val duplicatePolicyService: DuplicatePolicyService,
) {
    @PostMapping("/{undoToken}/undo")
    fun undo(
        @PathVariable undoToken: String,
        @RequestParam ownerId: UUID,
    ): DuplicateUndoResponse {
        val result = duplicatePolicyService.undo(ownerId = ownerId, undoToken = undoToken)
        return DuplicateUndoResponse(
            undone = result.undone,
            canonicalIdempotencyKey = result.canonicalIdempotencyKey,
        )
    }
}
