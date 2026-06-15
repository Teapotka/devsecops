package com.demo.devsecops.controller

import com.demo.devsecops.dto.OrderStatsResponse
import com.demo.devsecops.service.StatsService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.client.RestTestClient
import java.math.BigDecimal

@WebMvcTest(controllers = [StatsController::class])
@AutoConfigureRestTestClient
class StatsControllerTest {

    @Autowired
    private lateinit var restTestClient: RestTestClient

    @MockkBean
    private lateinit var statsService: StatsService

    @Test
    fun `GET api stats orders returns 200`() {
        every { statsService.getOrderStats() } returns OrderStatsResponse(
            totalOrders = 2,
            paidOrders = 1,
            cancelledOrders = 0,
            shippedOrders = 0,
            totalRevenue = BigDecimal("99.98"),
            cached = false
        )

        restTestClient.get()
            .uri("/api/stats/orders")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.totalOrders").isEqualTo(2)
            .jsonPath("$.paidOrders").isEqualTo(1)
            .jsonPath("$.cached").isEqualTo(false)
    }

    @Test
    fun `DELETE api stats cache returns 204`() {
        every { statsService.clearOrderStatsCache() } just runs

        restTestClient.delete()
            .uri("/api/stats/cache")
            .exchange()
            .expectStatus().isNoContent
    }
}
