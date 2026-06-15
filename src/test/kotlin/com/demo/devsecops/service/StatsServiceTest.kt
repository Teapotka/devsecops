package com.demo.devsecops.service

import com.demo.devsecops.dto.OrderStatsResponse
import com.demo.devsecops.entity.OrderStatus
import com.demo.devsecops.repository.OrderRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import java.math.BigDecimal

class StatsServiceTest {

    private val orderRepository: OrderRepository = mockk()
    private val cache: Cache = mockk(relaxed = true)
    private val cacheManager: CacheManager = mockk()
    private lateinit var statsService: StatsService

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { cacheManager.getCache(StatsService.ORDER_STATS_CACHE) } returns cache
        statsService = StatsService(orderRepository, cacheManager)
    }

    @Test
    fun `get order stats returns cached value on cache hit`() {
        val cachedStats = OrderStatsResponse(
            totalOrders = 5,
            paidOrders = 2,
            cancelledOrders = 1,
            shippedOrders = 2,
            totalRevenue = BigDecimal("100.00"),
            cached = false
        )

        every {
            cache.get(StatsService.ORDER_STATS_KEY, OrderStatsResponse::class.java)
        } returns cachedStats

        val result = statsService.getOrderStats()

        assertEquals(5, result.totalOrders)
        assertTrue(result.cached)
        verify(exactly = 0) { orderRepository.count() }
        verify(exactly = 0) { cache.put(any(), any()) }
    }

    @Test
    fun `get order stats calculates from database on cache miss`() {
        every {
            cache.get(StatsService.ORDER_STATS_KEY, OrderStatsResponse::class.java)
        } returns null
        every { orderRepository.count() } returns 3L
        every { orderRepository.countByStatus(OrderStatus.PAID) } returns 1L
        every { orderRepository.countByStatus(OrderStatus.CANCELLED) } returns 1L
        every { orderRepository.countByStatus(OrderStatus.SHIPPED) } returns 1L
        every { orderRepository.sumRevenueByStatus(OrderStatus.PAID) } returns BigDecimal("49.99")

        val result = statsService.getOrderStats()

        assertEquals(3, result.totalOrders)
        assertEquals(1, result.paidOrders)
        assertEquals(BigDecimal("49.99"), result.totalRevenue)
        assertFalse(result.cached)
        verify(exactly = 1) { cache.put(StatsService.ORDER_STATS_KEY, any()) }
    }

    @Test
    fun `clear order stats cache evicts cached entry`() {
        statsService.clearOrderStatsCache()

        verify(exactly = 1) { cache.evict(StatsService.ORDER_STATS_KEY) }
        verify(exactly = 0) { cache.clear() }
    }
}
