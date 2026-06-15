package com.demo.devsecops.service

import com.demo.devsecops.dto.CreateUserRequest
import com.demo.devsecops.dto.UserAccountResponse
import com.demo.devsecops.entity.UserAccount
import com.demo.devsecops.exception.DuplicateResourceException
import com.demo.devsecops.exception.ResourceNotFoundException
import com.demo.devsecops.repository.UserAccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserAccountService(private val userAccountRepository: UserAccountRepository) {

    private val logger = LoggerFactory.getLogger(UserAccountService::class.java)

    fun createUser(request: CreateUserRequest): UserAccountResponse {
        if (userAccountRepository.existsByEmail(request.email)) {
            logger.warn("Duplicate user creation attempt: email={}", request.email)
            throw DuplicateResourceException("User with this email already exists")
        }

        val now = LocalDateTime.now()

        val user = UserAccount(
            email = request.email,
            name = request.name,
            createdAt = now,
            updatedAt = now
        )

        val savedUser = userAccountRepository.save(user)
        logger.info("User created: id={}, email={}", savedUser.id, savedUser.email)
        return savedUser.toResponse()
    }

    fun getAllUsers(): List<UserAccountResponse> {
        return userAccountRepository.findAll()
            .map { it.toResponse() }
    }

    fun getUserById(id: UUID): UserAccountResponse {
        return userAccountRepository.findById(id)
            .orElseThrow {
                logger.warn("User not found: id={}", id)
                ResourceNotFoundException("User with this id is not found")
            }
            .toResponse()
    }

    fun deleteUser(id: UUID) {
        if (!userAccountRepository.existsById(id)) {
            logger.warn("User not found: id={}", id)
            throw ResourceNotFoundException("User with this id is not found")
        }
        userAccountRepository.deleteById(id)
        logger.info("User deleted: id={}", id)
    }

    private fun UserAccount.toResponse(): UserAccountResponse {
        return UserAccountResponse(
            id = id,
            email = email,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
