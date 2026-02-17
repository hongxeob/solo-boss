package com.soloboss.ai.web.v1.review

import com.soloboss.ai.application.review.ReviewService
import com.soloboss.ai.domain.interaction.ReviewTaskStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController(
    private val reviewService: ReviewService,
) {
    @GetMapping
    fun list(
        @RequestParam ownerId: UUID,
        @RequestParam(required = false) status: ReviewTaskStatus?,
        pageable: Pageable,
    ): Page<ReviewResponse> =
        reviewService
            .list(ownerId = ownerId, status = status, pageable = pageable)
            .map { it.toResponse() }

    @GetMapping("/{reviewTaskId}")
    fun get(
        @PathVariable reviewTaskId: UUID,
        @RequestParam ownerId: UUID,
    ): ReviewResponse = reviewService.get(ownerId = ownerId, reviewTaskId = reviewTaskId).toResponse()

    @PatchMapping("/{reviewTaskId}/resolve")
    fun resolve(
        @PathVariable reviewTaskId: UUID,
        @RequestParam ownerId: UUID,
        @RequestBody request: ResolveReviewRequest,
    ): ReviewResponse =
        reviewService
            .resolve(
                ownerId = ownerId,
                reviewTaskId = reviewTaskId,
                correctedPayload = request.correctedPayload,
            ).toResponse()
}
