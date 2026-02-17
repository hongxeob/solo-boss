package com.soloboss.ai.application.notification

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AlimtalkService(
    private val senderGateway: AlimtalkSenderGateway,
) : AlimtalkNotifier {
    fun send(command: AlimtalkSendCommand): AlimtalkSendResult {
        val missing = command.templateCode.requiredVariables - command.variables.keys
        if (missing.isNotEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "템플릿 변수 누락: ${missing.joinToString(",")}",
            )
        }
        return senderGateway.send(command)
    }

    override fun sendSafely(command: AlimtalkSendCommand?) {
        if (command == null) {
            return
        }
        runCatching { send(command) }
            .onFailure { logger.warn("알림톡 발송 실패 template={} to={} reason={}", command.templateCode, command.to, it.message) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AlimtalkService::class.java)
    }
}

fun interface AlimtalkNotifier {
    fun sendSafely(command: AlimtalkSendCommand?)
}
