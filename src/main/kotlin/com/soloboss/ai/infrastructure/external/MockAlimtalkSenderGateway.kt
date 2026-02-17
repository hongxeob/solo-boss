package com.soloboss.ai.infrastructure.external

import com.soloboss.ai.application.notification.AlimtalkSendCommand
import com.soloboss.ai.application.notification.AlimtalkSendResult
import com.soloboss.ai.application.notification.AlimtalkSenderGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MockAlimtalkSenderGateway : AlimtalkSenderGateway {
    override fun send(command: AlimtalkSendCommand): AlimtalkSendResult {
        logger.info(
            "[MOCK-ALIMTALK] template={} to={} variables={}",
            command.templateCode,
            command.to,
            command.variables,
        )
        return AlimtalkSendResult(
            templateCode = command.templateCode,
            to = command.to,
            status = "MOCK_SENT",
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MockAlimtalkSenderGateway::class.java)
    }
}
