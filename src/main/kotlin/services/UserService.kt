package com.lccuellar.services

import com.lccuellar.models.User
import com.lccuellar.repositories.UserRepository

fun hashPassword(password: String): String {
    return password
}

fun verifyPassword(password: String, passwordHash: String): Boolean {
    return passwordHash == hashPassword(password)
}

class UserService(private val repository: UserRepository) {
    suspend fun createUser(username: String, password: String): User {
        val hashedPassword = hashPassword(password)
        return repository.create(username, hashedPassword)
    }

    suspend fun validateUser(username: String, password: String): User? {
        val user = repository.findByUsername(username)
        return user?.takeIf {verifyPassword(password, it.password)}
    }

    suspend fun findByID(userID: Int): User? {
        return repository.findByID(userID)
    }
}