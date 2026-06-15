package com.demo.devsecops.controller

import com.demo.devsecops.dto.OrderResponse
import com.demo.devsecops.entity.OrderStatus
import com.demo.devsecops.exception.GlobalExceptionHandler
import com.demo.devsecops.exception.ResourceNotFoundException
import com.demo.devsecops.service.OrderService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@WebMvcTest(controllers = [OrderController::class])
@Import(GlobalExceptionHandler::class)
@AutoConfigureRestTestClient
class OrderControllerTest {

    @Autowired
    private lateinit var restTestClient: RestTestClient

    @MockkBean
    private lateinit var orderService: OrderService

    private val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val orderId = UUID.fromString("22222222-2222-2222-2222-222222222222")
    private val fixedTime = LocalDateTime.of(2026, 6, 14, 12, 0)

    @Test
    fun `POST api orders valid request returns 200`() {
        val response = OrderResponse(
            id = orderId,
            userAccountId = userId,
            productName = "Kotlin Book",
            quantity = 1,
            price = BigDecimal("29.99"),
            status = OrderStatus.CREATED,
            createdAt = fixedTime,
            updatedAt = fixedTime
        )

        every { orderService.createOrder(any()) } returns response

        restTestClient.post()
            .uri("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {
                  "userAccountId": "$userId",
                  "productName": "Kotlin Book",
                  "quantity": 1,
                  "price": 29.99
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(orderId.toString())
            .jsonPath("$.productName").isEqualTo("Kotlin Book")
    }

    @Test
    fun `POST api orders missing user returns 404`() {
        every { orderService.createOrder(any()) } throws ResourceNotFoundException(
            "User with this id is not found"
        )

        restTestClient.post()
            .uri("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {
                  "userAccountId": "$userId",
                  "productName": "Kotlin Book",
                  "quantity": 1,
                  "price": 29.99
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    @Test
    fun `POST api orders invalid body returns 400`() {
        restTestClient.post()
            .uri("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {
                  "userAccountId": "$userId",
                  "productName": "",
                  "quantity": 0,
                  "price": -1
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
    }

    @Test
    fun `PATCH api orders status success returns 200`() {
        val response = OrderResponse(
            id = orderId,
            userAccountId = userId,
            productName = "Kotlin Book",
            quantity = 1,
            price = BigDecimal("29.99"),
            status = OrderStatus.PAID,
            createdAt = fixedTime,
            updatedAt = fixedTime
        )

        every { orderService.updateOrderStatus(orderId, any()) } returns response

        restTestClient.patch()
            .uri("/api/orders/{id}/status", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"status":"PAID"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("PAID")
    }

    @Test
    fun `PATCH api orders status not found returns 404`() {
        every { orderService.updateOrderStatus(orderId, any()) } throws ResourceNotFoundException(
            "Order with this id is not found"
        )

        restTestClient.patch()
            .uri("/api/orders/{id}/status", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"status":"PAID"}""")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    @Test
    fun `GET api orders by user id not found returns 404`() {
        every { orderService.getOrdersByUserId(userId) } throws ResourceNotFoundException(
            "User with this id is not found"
        )

        restTestClient.get()
            .uri("/api/orders/user/{userId}", userId)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    @Test
    fun `DELETE api orders by id success returns 204`() {
        every { orderService.deleteOrder(orderId) } just runs

        restTestClient.delete()
            .uri("/api/orders/{id}", orderId)
            .exchange()
            .expectStatus().isNoContent
    }
}
