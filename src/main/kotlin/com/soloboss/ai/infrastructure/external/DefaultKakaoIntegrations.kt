package com.soloboss.ai.infrastructure.external

import com.soloboss.ai.application.integration.ChannelOwnerResolver
import com.soloboss.ai.application.integration.KakaoSignatureVerifier
import com.soloboss.ai.infrastructure.persistence.KakaoChannelRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.springframework.http.HttpStatus

@Component
class DefaultChannelOwnerResolver(
    private val kakaoChannelRepository: KakaoChannelRepository,
    @Value("\${kakao.webhook.allow-deterministic-owner-fallback:true}")
    private val allowDeterministicFallback: Boolean = true,
) : ChannelOwnerResolver {
    override fun resolveOwnerId(channelId: String): UUID {
        val mapped = kakaoChannelRepository.findByChannelIdAndIsActiveTrue(channelId)
        if (mapped != null) {
            return mapped.ownerId
        }

        if (allowDeterministicFallback) {
            return UUID.nameUUIDFromBytes(channelId.toByteArray(StandardCharsets.UTF_8))
        }

        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "등록되지 않은 카카오 채널입니다. channelId=$channelId")
    }
}

@Component
class DefaultKakaoSignatureVerifier(
    @Value("\${kakao.webhook.secret:}") private val webhookSecret: String = "",
) : KakaoSignatureVerifier {
    override fun isValid(
        signature: String,
        eventId: String,
        messageId: String,
        channelId: String,
    ): Boolean {
        if (signature.isBlank() || eventId.isBlank() || messageId.isBlank() || channelId.isBlank()) {
            return false
        }

        // Backward-compatible local mode: no secret configured.
        if (webhookSecret.isBlank()) {
            return true
        }

        val payload = "$eventId:$messageId:$channelId"
        val expected = hmacSha256Hex(webhookSecret, payload)
        val normalizedSignature = signature.removePrefix("sha256=").lowercase()
        return constantTimeEquals(expected, normalizedSignature)
    }

    private fun hmacSha256Hex(
        secret: String,
        payload: String,
    ): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val digest = mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun constantTimeEquals(
        a: String,
        b: String,
    ): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}
