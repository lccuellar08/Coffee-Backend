package services

import com.lccuellar.models.City
import com.lccuellar.repositories.CityRepository
import com.lccuellar.services.CoffeeShopService

class CityService(
    private val repository: CityRepository,
    private val coffeeShopRepository: CoffeeShopService
) {
    suspend fun createCity(name: String, state: String): City {
        return repository.create(name, state)
    }

    suspend fun updateCity(cityID: Int, name: String?, state: String?): City? {
        return repository.update(cityID, name, state)
    }

    suspend fun deleteCity(cityID: Int): Boolean {
        val coffeeShops = coffeeShopRepository.getCoffeeShopsInCity(cityID)
        for(coffeeShop in coffeeShops) {
            val deleted = coffeeShopRepository.deleteCoffeeShop(coffeeShop.id.value)
            if(!deleted) return false
        }

        return repository.delete(cityID)
    }

    suspend fun getCity(cityID: Int): City? {
        return repository.findByID(cityID)
    }

    suspend fun getAllCities(): List<City> {
        return repository.list()
    }
}