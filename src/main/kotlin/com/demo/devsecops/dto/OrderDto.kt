package com.demo.devsecops.dto

import com.demo.devsecops.entity.OrderStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val userAccountId: UUID,
    val productName: String,
    val quantity: Int,
    val price: BigDecimal,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateOrderRequest(
    @field:NotNull(message = "User id is required")
    val userAccountId: UUID,

    @field:NotBlank(message = "Product name is required")
    @field:Size(max = 180, message = "Product name must not exceed 180 characters")
    val productName: String,

    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int,

    @field:DecimalMin(value = "0.00", inclusive = true, message = "Price must be at least 0")
    val price: BigDecimal
)

data class UpdateOrderStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: OrderStatus
)