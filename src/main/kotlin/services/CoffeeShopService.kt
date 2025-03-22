package com.lccuellar.services

import com.lccuellar.models.CoffeeShop
import com.lccuellar.models.Score
import com.lccuellar.repositories.CoffeeShopRepository
import com.lccuellar.repositories.CoffeeShopWithScores
import com.lccuellar.repositories.ScoreRepository
import java.time.LocalDate


class CoffeeShopService(
    private val repository: CoffeeShopRepository,
    private val scoreRepository: ScoreRepository
) {
    suspend fun createCoffeeShop(cityID: Int, name: String, date: LocalDate,
                                 address: String): CoffeeShop? {
        return repository.create(cityID, name, date, address)
    }

    suspend fun updateCoffeeShop(coffeeShopID: Int, name: String?, date: LocalDate?, address: String?): CoffeeShop? {
        return repository.update(coffeeShopID, name, date, address)
    }

    suspend fun deleteCoffeeShop(coffeeShopID: Int): Boolean {
        val scores = scoreRepository.findByCoffeeShop(coffeeShopID)
        for(score in scores) {
            val deleted = scoreRepository.delete(score.id.value)
            if(!deleted) return false
        }

        return repository.delete(coffeeShopID)
    }

    suspend fun getCoffeeShopWithScores(coffeeShopID: Int): CoffeeShopWithScores? {
        val coffeeShop = repository.findByID(coffeeShopID) ?: return null
        val scores = scoreRepository.findByCoffeeShop(coffeeShopID) ?: emptyList()
        return CoffeeShopWithScores(coffeeShop, scores)
    }

    suspend fun getCoffeeShopsInCity(cityID: Int): List<CoffeeShop> {
        val coffeeShops = repository.findByCity(cityID)
        return coffeeShops
    }

    suspend fun getCoffeeShopsWithScoresInCity(cityID: Int): List<CoffeeShopWithScores> {
        val coffeeShopsWithScores = repository.findByCityWithScores(cityID)
        return coffeeShopsWithScores
    }

    suspend fun createCoffeeShopSCore(coffeeShopID: Int, userID: Int,
                                      scoreNum: Float, scoreType: String, notes: String?): Score {
        return scoreRepository.create(coffeeShopID, userID, scoreNum, scoreType, notes)
    }
}