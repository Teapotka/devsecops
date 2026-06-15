package com.demo.devsecops.repository

import com.demo.devsecops.entity.UserAccount
import com.demo.devsecops.support.RepositoryTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

class UserAccountRepositoryTest : RepositoryTestBase() {

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @Test
    fun `existsByEmail returns true when user exists`() {
        val email = "repo-user-${UUID.randomUUID()}@example.com"
        saveUser(email = email, name = "Repo User")

        assertTrue(userAccountRepository.existsByEmail(email))
    }

    @Test
    fun `existsByEmail returns false when user does not exist`() {
        assertFalse(userAccountRepository.existsByEmail("missing-${UUID.randomUUID()}@example.com"))
    }

    @Test
    fun `findByEmail returns saved user`() {
        val email = "find-user-${UUID.randomUUID()}@example.com"
        val saved = saveUser(email = email, name = "Find Me")

        val found = userAccountRepository.findByEmail(email)

        assertNotNull(found)
        assertEquals(saved.id, found!!.id)
        assertEquals(email, found.email)
        assertEquals("Find Me", found.name)
    }

    private fun saveUser(email: String, name: String): UserAccount {
        val now = LocalDateTime.now()
        return userAccountRepository.save(
            UserAccount(
                email = email,
                name = name,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
