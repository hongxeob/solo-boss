package com.soloboss.ai.web.v1.notification

import com.soloboss.ai.application.notification.AlimtalkService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val alimtalkService: AlimtalkService,
) {
    @PostMapping("/alimtalk")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendAlimtalk(
        @Valid @RequestBody request: AlimtalkSendRequest,
    ): AlimtalkSendResponse = alimtalkService.send(request.toCommand()).toResponse()
}
