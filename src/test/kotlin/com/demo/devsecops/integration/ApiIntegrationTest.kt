package com.demo.devsecops.integration

import com.demo.devsecops.dto.OrderResponse
import com.demo.devsecops.dto.UserAccountResponse
import com.demo.devsecops.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID

class ApiIntegrationTest : IntegrationTestBase() {

    private val missingOrderId = UUID.fromString("00000000-0000-0000-0000-000000000099")

    @Test
    fun `full user and order flow`() {
        val email = "e2e-${UUID.randomUUID()}@example.com"

        val userId = restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"email":"$email","name":"E2E User"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserAccountResponse::class.java)
            .returnResult()
            .responseBody!!
            .id

        val orderId = restTestClient.post()
            .uri("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {
                  "userAccountId": "$userId",
                  "productName": "E2E Product",
                  "quantity": 2,
                  "price": 99.50
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(OrderResponse::class.java)
            .returnResult()
            .responseBody!!
            .id

        restTestClient.get()
            .uri("/api/orders/{id}", orderId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(orderId.toString())

        restTestClient.patch()
            .uri("/api/orders/{id}/status", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"status":"PAID"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("PAID")

        restTestClient.get()
            .uri("/api/orders/user/{userId}", userId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(orderId.toString())

        restTestClient.delete()
            .uri("/api/orders/{id}", orderId)
            .exchange()
            .expectStatus().isNoContent

        restTestClient.delete()
            .uri("/api/users/{id}", userId)
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `duplicate user email returns 409`() {
        val email = "duplicate-${UUID.randomUUID()}@example.com"
        val body = """{"email":"$email","name":"Duplicate User"}"""

        restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isOk

        restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.status").isEqualTo(409)
            .jsonPath("$.message").isEqualTo("User with this email already exists")
    }

    @Test
    fun `missing order returns 404`() {
        restTestClient.get()
            .uri("/api/orders/{id}", missingOrderId)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.path").isEqualTo("/api/orders/$missingOrderId")
    }

    @Test
    fun `invalid request body returns 400`() {
        restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"email":"invalid-email","name":""}""")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.path").isEqualTo("/api/users")
    }
}
