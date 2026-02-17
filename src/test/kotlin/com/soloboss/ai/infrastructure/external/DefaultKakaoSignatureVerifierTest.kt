package com.soloboss.ai.infrastructure.external

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class DefaultKakaoSignatureVerifierTest {
    @Test
    fun `validates hmac signature when secret configured`() {
        val secret = "test-secret"
        val verifier = DefaultKakaoSignatureVerifier(secret)
        val payload = "evt:msg:ch"
        val signature = hmacSha256(secret, payload)

        val valid = verifier.isValid(signature, "evt", "msg", "ch")

        assertTrue(valid)
    }

    @Test
    fun `returns false when signature does not match`() {
        val verifier = DefaultKakaoSignatureVerifier("test-secret")

        val valid = verifier.isValid("deadbeef", "evt", "msg", "ch")

        assertFalse(valid)
    }

    private fun hmacSha256(
        secret: String,
        payload: String,
    ): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(payload.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
