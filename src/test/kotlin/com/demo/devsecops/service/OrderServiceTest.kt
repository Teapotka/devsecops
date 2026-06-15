package com.demo.devsecops.service

import com.demo.devsecops.dto.CreateOrderRequest
import com.demo.devsecops.dto.UpdateOrderStatusRequest
import com.demo.devsecops.entity.Order
import com.demo.devsecops.entity.OrderStatus
import com.demo.devsecops.entity.UserAccount
import com.demo.devsecops.exception.ResourceNotFoundException
import com.demo.devsecops.repository.OrderRepository
import com.demo.devsecops.repository.UserAccountRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class OrderServiceTest {

    private val orderRepository: OrderRepository = mockk()
    private val userAccountRepository: UserAccountRepository = mockk()
    private val statsService: StatsService = mockk(relaxed = true)
    private lateinit var orderService: OrderService

    private val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val orderId = UUID.fromString("22222222-2222-2222-2222-222222222222")
    private val fixedTime = LocalDateTime.of(2026, 6, 14, 12, 0)

    private val user = UserAccount(
        id = userId,
        email = "user@example.com",
        name = "Test User",
        createdAt = fixedTime,
        updatedAt = fixedTime
    )

    @BeforeEach
    fun setUp() {
        orderService = OrderService(orderRepository, userAccountRepository, statsService)
    }

    @Test
    fun `create order success`() {
        val request = CreateOrderRequest(
            userAccountId = userId,
            productName = "Kotlin Book",
            quantity = 1,
            price = BigDecimal("29.99")
        )
        val savedOrder = Order(
            id = orderId,
            userAccount = user,
            productName = request.productName,
            quantity = request.quantity,
            price = request.price,
            createdAt = fixedTime,
            updatedAt = fixedTime
        )

        every { userAccountRepository.findById(userId) } returns Optional.of(user)
        every { orderRepository.save(any()) } returns savedOrder

        val response = orderService.createOrder(request)

        assertEquals(orderId, response.id)
        assertEquals(userId, response.userAccountId)
        assertEquals(request.productName, response.productName)
        assertEquals(OrderStatus.CREATED, response.status)
        verify(exactly = 1) { statsService.clearOrderStatsCache() }
    }

    @Test
    fun `create order for missing user throws ResourceNotFoundException`() {
        val request = CreateOrderRequest(
            userAccountId = userId,
            productName = "Kotlin Book",
            quantity = 1,
            price = BigDecimal("29.99")
        )

        every { userAccountRepository.findById(userId) } returns Optional.empty()

        assertThrows(ResourceNotFoundException::class.java) {
            orderService.createOrder(request)
        }

        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `get order by id success`() {
        val order = sampleOrder()

        every { orderRepository.findById(orderId) } returns Optional.of(order)

        val response = orderService.getOrderById(orderId)

        assertEquals(orderId, response.id)
        assertEquals(userId, response.userAccountId)
    }

    @Test
    fun `get order by id not found throws ResourceNotFoundException`() {
        every { orderRepository.findById(orderId) } returns Optional.empty()

        assertThrows(ResourceNotFoundException::class.java) {
            orderService.getOrderById(orderId)
        }
    }

    @Test
    fun `get orders by user id success`() {
        val order = sampleOrder()

        every { userAccountRepository.existsById(userId) } returns true
        every { orderRepository.findByUserAccountId(userId) } returns listOf(order)

        val responses = orderService.getOrdersByUserId(userId)

        assertEquals(1, responses.size)
        assertEquals(orderId, responses.first().id)
    }

    @Test
    fun `get orders by user id when user missing throws ResourceNotFoundException`() {
        every { userAccountRepository.existsById(userId) } returns false

        assertThrows(ResourceNotFoundException::class.java) {
            orderService.getOrdersByUserId(userId)
        }

        verify(exactly = 0) { orderRepository.findByUserAccountId(any()) }
    }

    @Test
    fun `update order status success`() {
        val order = sampleOrder()
        val updatedOrder = sampleOrder(status = OrderStatus.PAID)

        every { orderRepository.findById(orderId) } returns Optional.of(order)
        every { orderRepository.save(any()) } returns updatedOrder

        val response = orderService.updateOrderStatus(
            orderId,
            UpdateOrderStatusRequest(status = OrderStatus.PAID)
        )

        assertEquals(OrderStatus.PAID, response.status)
        verify(exactly = 1) { orderRepository.save(any()) }
        verify(exactly = 1) { statsService.clearOrderStatsCache() }
    }

    @Test
    fun `update order status not found throws ResourceNotFoundException`() {
        every { orderRepository.findById(orderId) } returns Optional.empty()

        assertThrows(ResourceNotFoundException::class.java) {
            orderService.updateOrderStatus(
                orderId,
                UpdateOrderStatusRequest(status = OrderStatus.PAID)
            )
        }
    }

    @Test
    fun `delete order success`() {
        every { orderRepository.existsById(orderId) } returns true
        every { orderRepository.deleteById(orderId) } just runs

        orderService.deleteOrder(orderId)

        verify(exactly = 1) { orderRepository.deleteById(orderId) }
        verify(exactly = 1) { statsService.clearOrderStatsCache() }
    }

    @Test
    fun `delete order not found throws ResourceNotFoundException`() {
        every { orderRepository.existsById(orderId) } returns false

        assertThrows(ResourceNotFoundException::class.java) {
            orderService.deleteOrder(orderId)
        }

        verify(exactly = 0) { orderRepository.deleteById(any()) }
    }

    private fun sampleOrder(status: OrderStatus = OrderStatus.CREATED): Order {
        return Order(
            id = orderId,
            userAccount = user,
            productName = "Kotlin Book",
            quantity = 1,
            price = BigDecimal("29.99"),
            status = status,
            createdAt = fixedTime,
            updatedAt = fixedTime
        )
    }
}
