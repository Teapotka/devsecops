package com.demo.devsecops.integration

import com.demo.devsecops.dto.UserAccountResponse
import com.demo.devsecops.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID

class UserAccountIntegrationTest : IntegrationTestBase() {

    @Test
    fun `creates and retrieves user`() {
        val email = "user-${UUID.randomUUID()}@example.com"

        val userId = restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"email":"$email","name":"Integration User"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserAccountResponse::class.java)
            .returnResult()
            .responseBody!!
            .id

        restTestClient.get()
            .uri("/api/users/{id}", userId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId.toString())
            .jsonPath("$.email").isEqualTo(email)
            .jsonPath("$.name").isEqualTo("Integration User")

        restTestClient.get()
            .uri("/api/users")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[?(@.email == '$email')]").exists()
    }

    @Test
    fun `duplicate email returns 409`() {
        val email = "dup-user-${UUID.randomUUID()}@example.com"
        val body = """{"email":"$email","name":"User One"}"""

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
    }

    @Test
    fun `get missing user returns 404`() {
        val missingUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        restTestClient.get()
            .uri("/api/users/{id}", missingUserId)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    @Test
    fun `delete user returns 204`() {
        val email = "delete-${UUID.randomUUID()}@example.com"

        val userId = restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"email":"$email","name":"Delete Me"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserAccountResponse::class.java)
            .returnResult()
            .responseBody!!
            .id

        restTestClient.delete()
            .uri("/api/users/{id}", userId)
            .exchange()
            .expectStatus().isNoContent

        restTestClient.get()
            .uri("/api/users/{id}", userId)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `invalid create user request returns 400`() {
        restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"email":"bad","name":""}""")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
    }
}
