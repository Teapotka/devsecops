package com.demo.devsecops.controller

import com.demo.devsecops.dto.UserAccountResponse
import com.demo.devsecops.exception.DuplicateResourceException
import com.demo.devsecops.exception.GlobalExceptionHandler
import com.demo.devsecops.exception.ResourceNotFoundException
import com.demo.devsecops.service.UserAccountService
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
import java.time.LocalDateTime
import java.util.UUID

@WebMvcTest(controllers = [UserAccountController::class])
@Import(GlobalExceptionHandler::class)
@AutoConfigureRestTestClient
class UserAccountControllerTest {

    @Autowired
    private lateinit var restTestClient: RestTestClient

    @MockkBean
    private lateinit var userAccountService: UserAccountService

    private val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val fixedTime = LocalDateTime.of(2026, 6, 14, 12, 0)

    @Test
    fun `POST api users valid request returns 200`() {
        val response = UserAccountResponse(
            id = userId,
            email = "user@example.com",
            name = "Test User",
            createdAt = fixedTime,
            updatedAt = fixedTime
        )

        every { userAccountService.createUser(any()) } returns response

        restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"email":"user@example.com","name":"Test User"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId.toString())
            .jsonPath("$.email").isEqualTo("user@example.com")
    }

    @Test
    fun `POST api users duplicate email returns 409`() {
        every { userAccountService.createUser(any()) } throws DuplicateResourceException(
            "User with this email already exists"
        )

        restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"email":"duplicate@example.com","name":"Test User"}""")
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.status").isEqualTo(409)
            .jsonPath("$.message").isEqualTo("User with this email already exists")
    }

    @Test
    fun `POST api users invalid email returns 400`() {
        restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"email":"not-an-email","name":"Test User"}""")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
    }

    @Test
    fun `GET api users by id not found returns 404`() {
        every { userAccountService.getUserById(userId) } throws ResourceNotFoundException(
            "User with this id is not found"
        )

        restTestClient.get()
            .uri("/api/users/{id}", userId)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    @Test
    fun `DELETE api users by id success returns 204`() {
        every { userAccountService.deleteUser(userId) } just runs

        restTestClient.delete()
            .uri("/api/users/{id}", userId)
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `DELETE api users by id not found returns 404`() {
        every { userAccountService.deleteUser(userId) } throws ResourceNotFoundException(
            "User with this id is not found"
        )

        restTestClient.delete()
            .uri("/api/users/{id}", userId)
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }
}
