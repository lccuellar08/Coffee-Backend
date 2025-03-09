package com.lccuellar.repositories

import com.lccuellar.models.*
import dbQuery
import java.time.LocalDate

data class CoffeeShopWithScores(
    val coffeeShop: CoffeeShop,
    val scores: List<Score>
)
class CoffeeShopRepository {
    suspend fun create(cityID: Int, name: String, date: LocalDate, address: String): CoffeeShop? = dbQuery {
        CoffeeShop.new {
            city = City[cityID]
            this.name = name
            this.date = date
            this.address = address
        }
    }
    suspend fun findByID(id: Int): CoffeeShop? = dbQuery {
        CoffeeShop.findById(id)
    }

    suspend fun findByCity(cityId: Int): List<CoffeeShop> = dbQuery {
        CoffeeShop.find { CoffeeShops.cityID eq cityId }.toList()
    }

    suspend fun findByCityWithScores(cityId: Int): List<CoffeeShopWithScores> = dbQuery {
        // Get all coffee shops for this city
        val shops = CoffeeShop.find { CoffeeShops.cityID eq cityId }.toList()

        // For each shop, fetch its scores
        shops.map { shop ->
            val scores = Score.find { Scores.coffeeShopID eq shop.id }.toList()
            CoffeeShopWithScores(shop, scores)
        }
    }
}