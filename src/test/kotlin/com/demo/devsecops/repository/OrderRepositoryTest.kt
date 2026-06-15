package com.demo.devsecops.repository

import com.demo.devsecops.entity.Order
import com.demo.devsecops.entity.OrderStatus
import com.demo.devsecops.entity.UserAccount
import com.demo.devsecops.support.RepositoryTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class OrderRepositoryTest : RepositoryTestBase() {

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    private lateinit var user: UserAccount

    @BeforeEach
    fun setUpUser() {
        val now = LocalDateTime.now()
        user = userAccountRepository.save(
            UserAccount(
                email = "order-repo-${UUID.randomUUID()}@example.com",
                name = "Order Repo User",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    @Test
    fun `findByUserAccountId returns orders for user`() {
        saveOrder(productName = "Book", price = BigDecimal("19.99"))
        saveOrder(productName = "Course", price = BigDecimal("49.99"))

        val orders = orderRepository.findByUserAccountId(user.id)

        assertEquals(2, orders.size)
        assertTrue(orders.all { it.userAccount.id == user.id })
    }

    @Test
    fun `findByUserAccountId returns empty list when user has no orders`() {
        val otherUser = userAccountRepository.save(
            UserAccount(
                email = "no-orders-${UUID.randomUUID()}@example.com",
                name = "No Orders",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val orders = orderRepository.findByUserAccountId(otherUser.id)

        assertTrue(orders.isEmpty())
    }

    private fun saveOrder(productName: String, price: BigDecimal): Order {
        val now = LocalDateTime.now()
        return orderRepository.save(
            Order(
                userAccount = user,
                productName = productName,
                quantity = 1,
                price = price,
                status = OrderStatus.CREATED,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
