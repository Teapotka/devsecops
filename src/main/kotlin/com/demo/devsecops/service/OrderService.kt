package com.demo.devsecops.service

import com.demo.devsecops.dto.CreateOrderRequest
import com.demo.devsecops.dto.OrderResponse
import com.demo.devsecops.dto.UpdateOrderStatusRequest
import com.demo.devsecops.entity.Order
import com.demo.devsecops.exception.ResourceNotFoundException
import com.demo.devsecops.repository.OrderRepository
import com.demo.devsecops.repository.UserAccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userAccountRepository: UserAccountRepository,
    private val statsService: StatsService
) {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    fun createOrder(request: CreateOrderRequest): OrderResponse {
        val user = userAccountRepository.findById(request.userAccountId)
            .orElseThrow {
                logger.warn("User not found: id={}", request.userAccountId)
                ResourceNotFoundException("User with this id is not found")
            }

        val now = LocalDateTime.now()

        val order = Order(
            userAccount = user,
            productName = request.productName,
            quantity = request.quantity,
            price = request.price,
            createdAt = now,
            updatedAt = now
        )

        val savedOrder = orderRepository.save(order)
        statsService.clearOrderStatsCache()
        logger.info(
            "Order created: id={}, userId={}, total={}",
            savedOrder.id,
            savedOrder.userAccount.id,
            savedOrder.price
        )
        return savedOrder.toResponse()
    }

    fun getAllOrders(): List<OrderResponse> {
        return orderRepository.findAll()
            .map { it.toResponse() }
    }

    fun getOrderById(id: UUID): OrderResponse {
        val order = orderRepository.findById(id)
            .orElseThrow {
                logger.warn("Order not found: id={}", id)
                ResourceNotFoundException("Order with this id is not found")
            }

        return order.toResponse()
    }

    fun getOrdersByUserId(userId: UUID): List<OrderResponse> {
        if (!userAccountRepository.existsById(userId)) {
            logger.warn("User not found: id={}", userId)
            throw ResourceNotFoundException("User with this id is not found")
        }

        return orderRepository.findByUserAccountId(userId)
            .map { it.toResponse() }
    }

    fun updateOrderStatus(
        id: UUID,
        request: UpdateOrderStatusRequest
    ): OrderResponse {
        val order = orderRepository.findById(id)
            .orElseThrow {
                logger.warn("Order not found: id={}", id)
                ResourceNotFoundException("Order with this id is not found")
            }

        order.status = request.status
        order.updatedAt = LocalDateTime.now()

        val updatedOrder = orderRepository.save(order)
        statsService.clearOrderStatsCache()
        logger.info("Order status updated: orderId={}, status={}", updatedOrder.id, updatedOrder.status)
        return updatedOrder.toResponse()
    }

    fun deleteOrder(id: UUID) {
        if (!orderRepository.existsById(id)) {
            logger.warn("Order not found: id={}", id)
            throw ResourceNotFoundException("Order with this id is not found")
        }

        orderRepository.deleteById(id)
        statsService.clearOrderStatsCache()
        logger.info("Order deleted: id={}", id)
    }

    private fun Order.toResponse(): OrderResponse {
        return OrderResponse(
            id = id,
            userAccountId = userAccount.id,
            productName = productName,
            quantity = quantity,
            price = price,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
