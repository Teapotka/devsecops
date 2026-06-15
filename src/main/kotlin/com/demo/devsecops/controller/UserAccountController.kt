package com.demo.devsecops.controller

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.http.ResponseEntity
import com.demo.devsecops.dto.UserAccountResponse
import com.demo.devsecops.dto.CreateUserRequest
import com.demo.devsecops.service.UserAccountService
import java.util.UUID
import jakarta.validation.Valid

// REST Controller
@RestController
@RequestMapping("/api/users")
class UserAccountController(
    private val userAccountService: UserAccountService
){
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserAccountResponse>>{
        return ResponseEntity.ok(userAccountService.getAllUsers())
    }

    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: UUID
    ): ResponseEntity<UserAccountResponse>{
        return ResponseEntity.ok(userAccountService.getUserById(id))
    }

    @PostMapping
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserAccountResponse>{
        return ResponseEntity.ok(userAccountService.createUser(request))
    }

    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: UUID
    ): ResponseEntity<Void>{
        userAccountService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

}