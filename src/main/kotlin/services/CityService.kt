package services

import com.lccuellar.models.City
import com.lccuellar.repositories.CityRepository

class CityService(
    private val repository: CityRepository,
) {
    suspend fun createCity(name: String, state: String): City {
        return repository.create(name, state)
    }

    suspend fun getCity(cityID: Int): City? {
        return repository.findByID(cityID)
    }

    suspend fun getAllCities(): List<City> {
        return repository.list()
    }
}