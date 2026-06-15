package com.demo.devsecops.dto

import java.time.LocalDateTime
import java.util.UUID
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserAccountResponse(
    val id: UUID,
    val email: String,
    val name: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateUserRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Size(max = 255, message = "Email must not exceed 255 characters")
    val email: String,

    @field:NotBlank(message = "Name is required")
     @field:Size(max = 120, message = "Name  must not exceed 120 characters")
    val name: String
)