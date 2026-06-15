package com.demo.devsecops.integration

import com.demo.devsecops.dto.OrderResponse
import com.demo.devsecops.dto.OrderStatsResponse
import com.demo.devsecops.dto.UserAccountResponse
import com.demo.devsecops.support.IntegrationTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.math.BigDecimal
import java.util.UUID

class StatsIntegrationTest : IntegrationTestBase() {

    private lateinit var userId: UUID

    @BeforeEach
    fun setUp() {
        clearStatsCache()
        userId = createUser()
    }

    @Test
    fun `get order stats returns cache miss then cache hit`() {
        val baseline = getOrderStats().totalOrders
        createOrder(price = "29.99")

        val firstResponse = getOrderStats()

        assertEquals(baseline + 1, firstResponse.totalOrders)
        assertFalse(firstResponse.cached)

        val secondResponse = getOrderStats()

        assertEquals(baseline + 1, secondResponse.totalOrders)
        assertTrue(secondResponse.cached)
    }

    @Test
    fun `creating order invalidates stats cache`() {
        val baseline = getOrderStats().totalOrders
        getOrderStats()

        createOrder(price = "10.00")

        val statsAfterCreate = getOrderStats()

        assertEquals(baseline + 1, statsAfterCreate.totalOrders)
        assertFalse(statsAfterCreate.cached)
    }

    @Test
    fun `updating order status invalidates stats cache`() {
        val orderId = createOrder(price = "50.00")
        val statsBeforePaid = getOrderStats()
        getOrderStats()

        restTestClient.patch()
            .uri("/api/orders/{id}/status", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"status":"PAID"}""")
            .exchange()
            .expectStatus().isOk

        val statsAfterPaid = getOrderStats()

        assertEquals(statsBeforePaid.paidOrders + 1, statsAfterPaid.paidOrders)
        assertEquals(
            statsBeforePaid.totalRevenue + BigDecimal("50.00"),
            statsAfterPaid.totalRevenue
        )
        assertFalse(statsAfterPaid.cached)
    }

    @Test
    fun `deleting order invalidates stats cache`() {
        val baseline = getOrderStats().totalOrders
        val orderId = createOrder(price = "20.00")
        getOrderStats()

        restTestClient.delete()
            .uri("/api/orders/{id}", orderId)
            .exchange()
            .expectStatus().isNoContent

        val statsAfterDelete = getOrderStats()

        assertEquals(baseline, statsAfterDelete.totalOrders)
        assertFalse(statsAfterDelete.cached)
    }

    @Test
    fun `DELETE api stats cache clears cached stats`() {
        createOrder(price = "15.00")

        getOrderStats()
        assertTrue(getOrderStats().cached)

        restTestClient.delete()
            .uri("/api/stats/cache")
            .exchange()
            .expectStatus().isNoContent

        assertFalse(getOrderStats().cached)
    }

    private fun clearStatsCache() {
        restTestClient.delete()
            .uri("/api/stats/cache")
            .exchange()
            .expectStatus().isNoContent
    }

    private fun getOrderStats(): OrderStatsResponse {
        return restTestClient.get()
            .uri("/api/stats/orders")
            .exchange()
            .expectStatus().isOk
            .expectBody(OrderStatsResponse::class.java)
            .returnResult()
            .responseBody!!
    }

    private fun createUser(): UUID {
        val email = "stats-user-${UUID.randomUUID()}@example.com"

        return restTestClient.post()
            .uri("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body("""{"email":"$email","name":"Stats User"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserAccountResponse::class.java)
            .returnResult()
            .responseBody!!
            .id
    }

    private fun createOrder(price: String): UUID {
        return restTestClient.post()
            .uri("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {
                  "userAccountId": "$userId",
                  "productName": "Kotlin Book",
                  "quantity": 1,
                  "price": $price
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(OrderResponse::class.java)
            .returnResult()
            .responseBody!!
            .id
    }
}
