package com.demo.devsecops.repository

import com.demo.devsecops.entity.Order
import com.demo.devsecops.entity.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {

    fun findByUserAccountId(userAccountId: UUID): List<Order>

    fun findByStatus(status: OrderStatus): List<Order>

    fun countByStatus(status: OrderStatus): Long

    @Query("SELECT COALESCE(SUM(o.price * o.quantity), 0) FROM Order o WHERE o.status = :status")
    fun sumRevenueByStatus(@Param("status") status: OrderStatus): BigDecimal
}
