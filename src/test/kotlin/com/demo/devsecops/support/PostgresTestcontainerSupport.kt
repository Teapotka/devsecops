package com.demo.devsecops.support

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer

object PostgresTestcontainerSupport {

    val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:17")
        .withDatabaseName("devsecops")
        .withUsername("devsecops_user")
        .withPassword("devsecops_password")

    init {
        postgres.start()
    }

    @JvmStatic
    fun registerProperties(registry: DynamicPropertyRegistry) {
        registry.add("spring.datasource.url") { postgres.jdbcUrl }
        registry.add("spring.datasource.username") { postgres.username }
        registry.add("spring.datasource.password") { postgres.password }
        registry.add("spring.flyway.url") { postgres.jdbcUrl }
        registry.add("spring.flyway.user") { postgres.username }
        registry.add("spring.flyway.password") { postgres.password }
    }
}
