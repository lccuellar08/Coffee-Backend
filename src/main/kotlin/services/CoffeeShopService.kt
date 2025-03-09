package com.lccuellar.services

import com.lccuellar.models.CoffeeShop
import com.lccuellar.models.Score
import com.lccuellar.repositories.CoffeeShopRepository
import com.lccuellar.repositories.ScoreRepository
import java.time.LocalDate

data class CoffeeShopWithScores(val coffeeShop: CoffeeShop, val scores: List<Score>)

class CoffeeShopService(
    private val repository: CoffeeShopRepository,
    private val scoreRepository: ScoreRepository
) {
    suspend fun createCoffeeShop(cityID: Int, name: String, date: LocalDate,
                                 address: String): CoffeeShop? {
        return repository.create(cityID, name, date, address)
    }

    suspend fun getCoffeeShopWithScores(coffeeShopID: Int): CoffeeShopWithScores? {
        val coffeeShop = repository.findByID(coffeeShopID) ?: return null
        val scores = scoreRepository.findByCoffeeShop(coffeeShopID) ?: emptyList()
        return CoffeeShopWithScores(coffeeShop, scores)
    }

    suspend fun getCoffeeShopsInCity(cityID: Int, withScores: Boolean): List<CoffeeShop> {
        val coffeeShops = repository.findByCity(cityID, withScores)
        return coffeeShops
    }

    suspend fun createCoffeeShopSCore(coffeeShopID: Int, userID: Int,
                                      scoreNum: Float, scoreType: String, notes: String?): Score {
        return scoreRepository.create(coffeeShopID, userID, scoreNum, scoreType, notes)
    }
}