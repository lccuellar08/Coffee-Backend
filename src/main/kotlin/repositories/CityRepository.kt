package com.lccuellar.repositories

import com.lccuellar.models.City
import com.lccuellar.models.Cities
import dbQuery

class CityRepository {
    suspend fun create(name: String, state: String): City = dbQuery {
        City.new {
            this.name = name
            this.state = state
        }
    }

    suspend fun findByID(id: Int): City? = dbQuery {
        City.findById(id)
    }

    suspend fun list(): List<City> = dbQuery {
        City.all().toList()
    }
}