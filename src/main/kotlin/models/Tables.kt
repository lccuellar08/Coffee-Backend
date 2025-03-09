package com.lccuellar.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object Users : IntIdTable() {
    val username = varchar("username", 255).uniqueIndex()
    val password = varchar("password", 255)
}

object Cities: IntIdTable() {
    val name = varchar("name", 255)
    val state = varchar("state", 50)
}

object CoffeeShops: IntIdTable() {
    val cityID = reference("city_id", Cities)
    val name = varchar("name", 255)
    val date = date("date")
    val address = varchar("address", 255)
}

object Scores: IntIdTable() {
    val userID = reference("user_id", Users)
    val coffeeShopID = reference("coffeeshop_id", CoffeeShops)
    val scoreNum = float("score_num")
    val scoreType = varchar("score_type", 50)
    val notes = text("notes").nullable()
}