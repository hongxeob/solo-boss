package com.soloboss.ai.application.customer

import com.soloboss.ai.domain.customer.Customer
import com.soloboss.ai.domain.customer.CustomerSource
import com.soloboss.ai.infrastructure.persistence.CustomerRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID

class CustomerServiceTest {
    private val repository = Mockito.mock(CustomerRepository::class.java)
    private val service = CustomerService(repository)

    @Test
    fun `list uses name filter when query exists`() {
        val ownerId = UUID.randomUUID()
        val pageable = PageRequest.of(0, 20)
        val customer = customer(ownerId = ownerId, name = "홍길동")
        Mockito
            .`when`(repository.findByOwnerIdAndNameContaining(ownerId, "홍", pageable))
            .thenReturn(PageImpl(listOf(customer)))

        val result = service.list(ownerId = ownerId, query = "홍", pageable = pageable)

        assertEquals(1, result.totalElements)
        Mockito.verify(repository).findByOwnerIdAndNameContaining(ownerId, "홍", pageable)
        Mockito.verify(repository, Mockito.never()).findByOwnerId(ownerId, pageable)
    }

    @Test
    fun `update throws when customer not owned by caller`() {
        val ownerId = UUID.randomUUID()
        val anotherOwnerId = UUID.randomUUID()
        val customerId = UUID.randomUUID()

        Mockito.`when`(repository.findById(customerId)).thenReturn(Optional.of(customer(ownerId = anotherOwnerId)))

        assertThrows(EntityNotFoundException::class.java) {
            service.update(
                ownerId = ownerId,
                customerId = customerId,
                command = UpdateCustomerCommand(name = "새 이름"),
            )
        }
    }

    @Test
    fun `delete removes customer when owner matches`() {
        val ownerId = UUID.randomUUID()
        val customerId = UUID.randomUUID()
        Mockito.`when`(repository.findById(customerId)).thenReturn(Optional.of(customer(id = customerId, ownerId = ownerId)))

        service.delete(ownerId = ownerId, customerId = customerId)

        val captor = ArgumentCaptor.forClass(Customer::class.java)
        Mockito.verify(repository).delete(captor.capture())
        assertEquals(customerId, captor.value.id)
    }

    private fun customer(
        id: UUID = UUID.randomUUID(),
        ownerId: UUID,
        name: String = "고객",
    ): Customer =
        Customer(
            id = id,
            ownerId = ownerId,
            name = name,
            source = CustomerSource.MANUAL,
        )
}
