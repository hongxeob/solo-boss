package com.soloboss.ai.infrastructure.external

import com.soloboss.ai.application.integration.ChannelOwnerResolver
import com.soloboss.ai.application.integration.KakaoSignatureVerifier
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.UUID

@Component
class DefaultChannelOwnerResolver : ChannelOwnerResolver {
    override fun resolveOwnerId(channelId: String): UUID = UUID.nameUUIDFromBytes(channelId.toByteArray(StandardCharsets.UTF_8))
}

@Component
class DefaultKakaoSignatureVerifier : KakaoSignatureVerifier {
    override fun isValid(
        signature: String,
        eventId: String,
        messageId: String,
        channelId: String,
    ): Boolean = signature.isNotBlank() && eventId.isNotBlank() && messageId.isNotBlank() && channelId.isNotBlank()
}
