package com.soloboss.ai.infrastructure.external

import com.soloboss.ai.domain.integration.KakaoChannel
import com.soloboss.ai.infrastructure.persistence.KakaoChannelRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class DefaultChannelOwnerResolverTest {
    private val repository = Mockito.mock(KakaoChannelRepository::class.java)

    @Test
    fun `returns mapped owner id when channel is registered`() {
        val ownerId = UUID.randomUUID()
        Mockito.`when`(repository.findByChannelIdAndIsActiveTrue("ch_1")).thenReturn(
            KakaoChannel(
                ownerId = ownerId,
                channelId = "ch_1",
                isActive = true,
            ),
        )
        val resolver =
            DefaultChannelOwnerResolver(
                kakaoChannelRepository = repository,
                allowDeterministicFallback = false,
            )

        val resolved = resolver.resolveOwnerId("ch_1")

        assertEquals(ownerId, resolved)
    }

    @Test
    fun `falls back to deterministic owner when allowed`() {
        Mockito.`when`(repository.findByChannelIdAndIsActiveTrue("ch_2")).thenReturn(null)
        val resolver =
            DefaultChannelOwnerResolver(
                kakaoChannelRepository = repository,
                allowDeterministicFallback = true,
            )

        val resolved = resolver.resolveOwnerId("ch_2")

        assertEquals(UUID.nameUUIDFromBytes("ch_2".toByteArray()), resolved)
    }

    @Test
    fun `throws bad request when channel not mapped and fallback disabled`() {
        Mockito.`when`(repository.findByChannelIdAndIsActiveTrue("ch_3")).thenReturn(null)
        val resolver =
            DefaultChannelOwnerResolver(
                kakaoChannelRepository = repository,
                allowDeterministicFallback = false,
            )

        assertThrows(ResponseStatusException::class.java) {
            resolver.resolveOwnerId("ch_3")
        }
    }
}
