package com.lccuellar.models


import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<User>(Users)
    var username by Users.username
    var password by Users.password
}

class City(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<City>(Cities)
    var name by Cities.name
    var state by Cities.state
}

class CoffeeShop(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<CoffeeShop>(CoffeeShops)
    var city by City referencedOn CoffeeShops.cityID
    var name by CoffeeShops.name
    var date by CoffeeShops.date
    var address by CoffeeShops.address
}

class Score(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Score>(Scores)
    var user by User referencedOn Scores.userID
    var coffeeShop by CoffeeShop referencedOn Scores.coffeeShopID
    var scoreNum by Scores.scoreNum
    var scoreType by Scores.scoreType
    var notes by Scores.notes
}