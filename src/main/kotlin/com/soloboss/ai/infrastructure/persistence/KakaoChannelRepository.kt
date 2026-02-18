package com.soloboss.ai.infrastructure.persistence

import com.soloboss.ai.domain.integration.KakaoChannel
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface KakaoChannelRepository : JpaRepository<KakaoChannel, UUID> {
    fun findByChannelIdAndIsActiveTrue(channelId: String): KakaoChannel?
}
