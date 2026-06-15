package com.demo.devsecops.repository

import com.demo.devsecops.entity.UserAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserAccountRepository : JpaRepository<UserAccount, UUID> {

    fun findByEmail(email: String): UserAccount?

    fun existsByEmail(email: String): Boolean
}