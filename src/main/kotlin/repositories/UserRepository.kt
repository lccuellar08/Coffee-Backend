package com.lccuellar.repositories

import com.lccuellar.models.User
import com.lccuellar.models.Users
import dbQuery

class UserRepository {
    suspend fun create(username: String, hashedPassword: String): User = dbQuery {
        User.new {
            this.username = username
            this.password = hashedPassword
        }
    }

    suspend fun findByID(id: Int): User? = dbQuery {
        User.findById(id)
    }

    suspend fun findByUsername(username: String): User? = dbQuery {
        User.find { Users.username eq username}.firstOrNull()
    }
}