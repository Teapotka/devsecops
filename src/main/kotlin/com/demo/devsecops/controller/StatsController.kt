package com.demo.devsecops.controller

import com.demo.devsecops.dto.OrderStatsResponse
import com.demo.devsecops.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/stats")
class StatsController(
    private val statsService: StatsService
) {

    @GetMapping("/orders")
    fun getOrderStats(): ResponseEntity<OrderStatsResponse> {
        return ResponseEntity.ok(statsService.getOrderStats())
    }

    @DeleteMapping("/cache")
    fun clearOrderStatsCache(): ResponseEntity<Void> {
        statsService.clearOrderStatsCache()
        return ResponseEntity.noContent().build()
    }
}
