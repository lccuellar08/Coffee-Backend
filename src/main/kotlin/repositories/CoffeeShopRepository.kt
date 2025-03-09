package com.lccuellar.repositories

import com.lccuellar.models.City
import com.lccuellar.models.CoffeeShop
import com.lccuellar.models.CoffeeShops
import com.lccuellar.models.Scores
import dbQuery
import java.time.LocalDate

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

    suspend fun findByCity(cityID: Int, withScores: Boolean): List<CoffeeShop> = dbQuery {
        if(withScores) {
            println("Querying with scores")
            val query = CoffeeShops.innerJoin(Scores)
                .select(CoffeeShops.columns + Scores.columns)
                .where {
                    CoffeeShops.cityID eq cityID
                }
            CoffeeShop.wrapRows(query).toList()
        } else {
            CoffeeShop.find { CoffeeShops.cityID eq cityID }.toList()
        }
    }
}