package com.demo.devsecops.controller

import com.demo.devsecops.dto.CreateOrderRequest
import com.demo.devsecops.dto.OrderResponse
import com.demo.devsecops.dto.UpdateOrderStatusRequest
import com.demo.devsecops.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    fun createOrder(
        @Valid @RequestBody request: CreateOrderRequest
    ): ResponseEntity<OrderResponse> {
        return ResponseEntity.ok(orderService.createOrder(request))
    }

    @GetMapping
    fun getAllOrders(): ResponseEntity<List<OrderResponse>> {
        return ResponseEntity.ok(orderService.getAllOrders())
    }

    @GetMapping("/{id}")
    fun getOrderById(
        @PathVariable id: UUID
    ): ResponseEntity<OrderResponse> {
        return ResponseEntity.ok(orderService.getOrderById(id))
    }

    @GetMapping("/user/{userId}")
    fun getOrdersByUserId(
        @PathVariable userId: UUID
    ): ResponseEntity<List<OrderResponse>> {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId))
    }

    @PatchMapping("/{id}/status")
    fun updateOrderStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<OrderResponse> {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteOrder(
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        orderService.deleteOrder(id)
        return ResponseEntity.noContent().build()
    }
}