package com.demo.devsecops.exception

import com.demo.devsecops.controller.UserAccountController
import com.demo.devsecops.dto.CreateUserRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import jakarta.servlet.http.HttpServletRequest

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()
    private val request: HttpServletRequest = mockk()

    @BeforeEach
    fun setUp() {
        every { request.requestURI } returns "/api/users"
    }

    @Test
    fun `ResourceNotFoundException returns 404 error response`() {
        val response = handler.handleResourceNotFoundException(
            ResourceNotFoundException("User with this id is not found"),
            request
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertErrorShape(
            body = response.body!!,
            status = 404,
            error = "Not Found",
            message = "User with this id is not found",
            path = "/api/users"
        )
    }

    @Test
    fun `DuplicateResourceException returns 409 error response`() {
        every { request.requestURI } returns "/api/users"

        val response = handler.handleDuplicateResourceException(
            DuplicateResourceException("User with this email already exists"),
            request
        )

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertErrorShape(
            body = response.body!!,
            status = 409,
            error = "Conflict",
            message = "User with this email already exists",
            path = "/api/users"
        )
    }

    @Test
    fun `MethodArgumentNotValidException returns 400 error response`() {
        every { request.requestURI } returns "/api/users"

        val bindingResult = BeanPropertyBindingResult(
            CreateUserRequest(email = "not-an-email", name = ""),
            "createUserRequest"
        )
        bindingResult.rejectValue("email", "Email", "Email must be valid")
        bindingResult.rejectValue("name", "NotBlank", "Name is required")

        val method = UserAccountController::class.java.getMethod(
            "createUser",
            CreateUserRequest::class.java
        )
        val methodParameter = MethodParameter(method, 0)
        val exception = MethodArgumentNotValidException(methodParameter, bindingResult)

        val response = handler.handleValidationException(exception, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body?.timestamp)
        assertEquals(400, response.body?.status)
        assertEquals("Bad Request", response.body?.error)
        assertEquals("/api/users", response.body?.path)
        assertEquals(
            "email: Email must be valid, name: Name is required",
            response.body?.message
        )
    }

    @Test
    fun `unexpected Exception returns 500 error response`() {
        every { request.requestURI } returns "/api/orders"

        val response = handler.handleGenericException(
            RuntimeException("Unexpected failure"),
            request
        )

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertErrorShape(
            body = response.body!!,
            status = 500,
            error = "Internal Server Error",
            message = "Unexpected failure",
            path = "/api/orders"
        )
    }

    private fun assertErrorShape(
        body: com.demo.devsecops.dto.ErrorResponse,
        status: Int,
        error: String,
        message: String,
        path: String
    ) {
        assertNotNull(body.timestamp)
        assertEquals(status, body.status)
        assertEquals(error, body.error)
        assertEquals(message, body.message)
        assertEquals(path, body.path)
    }
}
