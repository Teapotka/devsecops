package com.demo.devsecops.support

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertySource

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(DataSourceAutoConfiguration::class, FlywayAutoConfiguration::class)
abstract class RepositoryTestBase {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: org.springframework.test.context.DynamicPropertyRegistry) {
            PostgresTestcontainerSupport.registerProperties(registry)
        }
    }
}
