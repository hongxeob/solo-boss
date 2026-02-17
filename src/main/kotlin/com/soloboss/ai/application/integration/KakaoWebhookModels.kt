package com.soloboss.ai.application.integration

import com.soloboss.ai.application.ocr.OcrExtractResult

data class KakaoWebhookCommand(
    val eventId: String,
    val messageId: String,
    val channelId: String,
    val kakaoUserKey: String,
    val messageType: String,
    val mediaUrl: String,
    val signature: String,
)

typealias KakaoWebhookResult = OcrExtractResult

fun interface ChannelOwnerResolver {
    fun resolveOwnerId(channelId: String): java.util.UUID
}

fun interface KakaoSignatureVerifier {
    fun isValid(
        signature: String,
        eventId: String,
        messageId: String,
        channelId: String,
    ): Boolean
}
