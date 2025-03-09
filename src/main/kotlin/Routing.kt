package com.lccuellar


import com.lccuellar.repositories.CityRepository
import com.lccuellar.repositories.CoffeeShopRepository
import com.lccuellar.repositories.ScoreRepository
import com.lccuellar.repositories.UserRepository
import com.lccuellar.routes.userRoutes
import com.lccuellar.services.CoffeeShopService
import com.lccuellar.services.UserService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import routes.cityRoutes
import routes.rootRoutes
import services.CityService


fun Application.configureRouting() {
    routing {
        val userService = UserService(UserRepository())
        val cityService = CityService(CityRepository())
        val coffeeShopService = CoffeeShopService(CoffeeShopRepository(), ScoreRepository())

        rootRoutes()
        userRoutes(userService)
        cityRoutes(cityService, coffeeShopService)
    }
}