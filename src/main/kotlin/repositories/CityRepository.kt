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

    suspend fun update(cityID: Int, name: String?, state: String?): City? = dbQuery {
        City.findByIdAndUpdate(cityID) {
            it.name = name ?: it.name
            it.state = state ?: it.state
        }
    }

    suspend fun delete(cityID: Int): Boolean {
        val city = findByID(cityID) ?: return false
        dbQuery {
            city.delete()
        }
        return true
    }

    suspend fun findByID(id: Int): City? = dbQuery {
        City.findById(id)
    }

    suspend fun list(): List<City> = dbQuery {
        City.all().toList()
    }
}