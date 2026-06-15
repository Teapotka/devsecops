package com.demo.devsecops.support

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.client.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureRestTestClient
abstract class IntegrationTestBase {

    @Autowired
    protected lateinit var restTestClient: RestTestClient

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: org.springframework.test.context.DynamicPropertyRegistry) {
            PostgresTestcontainerSupport.registerProperties(registry)
        }
    }
}
