package com.demo.devsecops.dto

import java.math.BigDecimal

data class OrderStatsResponse(
    val totalOrders: Long,
    val paidOrders: Long,
    val cancelledOrders: Long,
    val shippedOrders: Long,
    val totalRevenue: BigDecimal,
    val cached: Boolean
)
