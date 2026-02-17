package com.soloboss.ai.infrastructure.persistence

import com.soloboss.ai.domain.customer.Customer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CustomerRepository : JpaRepository<Customer, UUID> {
    fun findByOwnerIdAndKakaoUserKey(
        ownerId: UUID,
        kakaoUserKey: String,
    ): Customer?

    fun findByOwnerId(
        ownerId: UUID,
        pageable: Pageable,
    ): Page<Customer>

    fun findByOwnerIdAndNameContaining(
        ownerId: UUID,
        name: String,
        pageable: Pageable,
    ): Page<Customer>
}
