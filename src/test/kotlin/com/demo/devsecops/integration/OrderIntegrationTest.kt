package com.demo.devsecops.integration

import com.demo.devsecops.dto.OrderResponse
import com.demo.devsecops.dto.UserAccountResponse
import com.demo.devsecops.support.IntegrationTestBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID

class OrderIntegrationTest : IntegrationTestBase() {

    private lateinit var userId: UUID

    @BeforeEach
    fun createUser() {
        val email = "order-user-${UUID.randomUUID()}@example.com"

        userId = restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"email":"$email","name":"Order User"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserAccountResponse::class.java)
            .returnResult()
            .responseBody!!
            .id
    }

    @Test
    fun `creates and retrieves order`() {
        val orderId = createOrder()

        restTestClient.get()
            .uri("/api/orders/{id}", orderId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(orderId.toString())
            .jsonPath("$.userAccountId").isEqualTo(userId.toString())
            .jsonPath("$.productName").isEqualTo("Kotlin Book")
    }

    @Test
    fun `updates order status`() {
        val orderId = createOrder()

        restTestClient.patch()
            .uri("/api/orders/{id}/status", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"status":"SHIPPED"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("SHIPPED")
    }

    @Test
    fun `gets orders by user id`() {
        val orderId = createOrder()

        restTestClient.get()
            .uri("/api/orders/user/{userId}", userId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[?(@.id == '$orderId')]").exists()
    }

    @Test
    fun `create order for missing user returns 404`() {
        val missingUserId = UUID.fromString("00000000-0000-0000-0000-000000000002")

        restTestClient.post()
            .uri("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {
                  "userAccountId": "$missingUserId",
                  "productName": "Kotlin Book",
                  "quantity": 1,
                  "price": 10.00
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    @Test
    fun `get missing order returns 404`() {
        val missingOrderId = UUID.fromString("00000000-0000-0000-0000-000000000003")

        restTestClient.get()
            .uri("/api/orders/{id}", missingOrderId)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    @Test
    fun `invalid create order request returns 400`() {
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
    fun `delete order returns 204`() {
        val orderId = createOrder()

        restTestClient.delete()
            .uri("/api/orders/{id}", orderId)
            .exchange()
            .expectStatus().isNoContent

        restTestClient.get()
            .uri("/api/orders/{id}", orderId)
            .exchange()
            .expectStatus().isNotFound
    }

    private fun createOrder(): UUID {
        return restTestClient.post()
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
            .expectBody(OrderResponse::class.java)
            .returnResult()
            .responseBody!!
            .id
    }
}
