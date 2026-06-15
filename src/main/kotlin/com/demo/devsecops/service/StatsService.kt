package com.demo.devsecops.service

import com.demo.devsecops.dto.OrderStatsResponse
import com.demo.devsecops.entity.OrderStatus
import com.demo.devsecops.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class StatsService(
    private val orderRepository: OrderRepository,
    private val cacheManager: CacheManager
) {

    private val logger = LoggerFactory.getLogger(StatsService::class.java)

    fun getOrderStats(): OrderStatsResponse {
        val cache = cacheManager.getCache(ORDER_STATS_CACHE)
        val cachedValue = cache?.get(ORDER_STATS_KEY, OrderStatsResponse::class.java)

        if (cachedValue != null) {
            logger.info("Order stats cache hit")
            return cachedValue.copy(cached = true)
        }

        logger.info("Order stats cache miss, calculating from database")
        val stats = calculateOrderStats()
        cache?.put(ORDER_STATS_KEY, stats)
        return stats
    }

    fun clearOrderStatsCache() {
        cacheManager.getCache(ORDER_STATS_CACHE)?.evict(ORDER_STATS_KEY)
        logger.info("Order stats cache cleared")
    }

    private fun calculateOrderStats(): OrderStatsResponse {
        return OrderStatsResponse(
            totalOrders = orderRepository.count(),
            paidOrders = orderRepository.countByStatus(OrderStatus.PAID),
            cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED),
            shippedOrders = orderRepository.countByStatus(OrderStatus.SHIPPED),
            totalRevenue = orderRepository.sumRevenueByStatus(OrderStatus.PAID),
            cached = false
        )
    }

    companion object {
        const val ORDER_STATS_CACHE = "orderStats"
        const val ORDER_STATS_KEY = "all"
    }
}
