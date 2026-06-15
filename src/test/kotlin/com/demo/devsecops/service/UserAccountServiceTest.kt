package com.demo.devsecops.service

import com.demo.devsecops.dto.CreateUserRequest
import com.demo.devsecops.entity.UserAccount
import com.demo.devsecops.exception.DuplicateResourceException
import com.demo.devsecops.exception.ResourceNotFoundException
import com.demo.devsecops.repository.UserAccountRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class UserAccountServiceTest {

    private val userAccountRepository: UserAccountRepository = mockk()
    private lateinit var userAccountService: UserAccountService

    private val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val fixedTime = LocalDateTime.of(2026, 6, 14, 12, 0)

    @BeforeEach
    fun setUp() {
        userAccountService = UserAccountService(userAccountRepository)
    }

    @Test
    fun `create user success`() {
        val request = CreateUserRequest(email = "user@example.com", name = "Test User")
        val savedUser = UserAccount(
            id = userId,
            email = request.email,
            name = request.name,
            createdAt = fixedTime,
            updatedAt = fixedTime
        )

        every { userAccountRepository.existsByEmail(request.email) } returns false
        every { userAccountRepository.save(any()) } returns savedUser

        val response = userAccountService.createUser(request)

        assertEquals(userId, response.id)
        assertEquals(request.email, response.email)
        assertEquals(request.name, response.name)
        verify(exactly = 1) { userAccountRepository.save(any()) }
    }

    @Test
    fun `duplicate email throws DuplicateResourceException`() {
        val request = CreateUserRequest(email = "duplicate@example.com", name = "Duplicate User")

        every { userAccountRepository.existsByEmail(request.email) } returns true

        assertThrows(DuplicateResourceException::class.java) {
            userAccountService.createUser(request)
        }

        verify(exactly = 0) { userAccountRepository.save(any()) }
    }

    @Test
    fun `get user by id success`() {
        val user = UserAccount(
            id = userId,
            email = "user@example.com",
            name = "Test User",
            createdAt = fixedTime,
            updatedAt = fixedTime
        )

        every { userAccountRepository.findById(userId) } returns Optional.of(user)

        val response = userAccountService.getUserById(userId)

        assertEquals(userId, response.id)
        assertEquals(user.email, response.email)
    }

    @Test
    fun `get user by id not found throws ResourceNotFoundException`() {
        every { userAccountRepository.findById(userId) } returns Optional.empty()

        assertThrows(ResourceNotFoundException::class.java) {
            userAccountService.getUserById(userId)
        }
    }

    @Test
    fun `delete user success`() {
        every { userAccountRepository.existsById(userId) } returns true
        every { userAccountRepository.deleteById(userId) } just runs

        userAccountService.deleteUser(userId)

        verify(exactly = 1) { userAccountRepository.deleteById(userId) }
    }

    @Test
    fun `delete user not found throws ResourceNotFoundException`() {
        every { userAccountRepository.existsById(userId) } returns false

        assertThrows(ResourceNotFoundException::class.java) {
            userAccountService.deleteUser(userId)
        }

        verify(exactly = 0) { userAccountRepository.deleteById(any()) }
    }
}
