package com.lccuellar.repositories

import com.lccuellar.models.Score
import com.lccuellar.models.Scores
import com.lccuellar.models.User
import com.lccuellar.models.CoffeeShop
import dbQuery

class ScoreRepository {
    suspend fun create(userID: Int, coffeeShopID: Int, scoreNum: Float, scoreType: String, notes: String?): Score = dbQuery {
        Score.new {
            user = User[userID]
            coffeeShop = CoffeeShop[coffeeShopID]
            this.scoreNum = scoreNum
            this.scoreType = scoreType
            this.notes = notes
        }
    }

    suspend fun update(scoreID: Int, scoreNum: Float?, scoreType: String?, notes: String?): Score? = dbQuery {
        Score.findByIdAndUpdate(scoreID) {
            it.scoreNum = scoreNum ?: it.scoreNum
            it.scoreType = scoreType ?: it.scoreType
            it.notes = scoreType ?: it.notes
        }
    }

    suspend fun delete(scoreID: Int): Boolean {
        val score = findByID(scoreID) ?: return false
        dbQuery {
            score.delete()
        }
        return true
    }

    suspend fun findByCoffeeShop(coffeeShopID: Int): List<Score> = dbQuery {
        Score.find {Scores.coffeeShopID eq coffeeShopID }.toList()
    }

    suspend fun findByUser(userID: Int): List<Score> = dbQuery {
        Score.find { Scores.userID eq userID }.toList()
    }

    suspend fun findByID(scoreID: Int): Score? = dbQuery {
        Score.findById(scoreID)
    }
}