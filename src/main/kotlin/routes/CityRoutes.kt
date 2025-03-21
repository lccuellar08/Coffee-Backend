package routes

import com.lccuellar.models.City
import com.lccuellar.models.CoffeeShop
import com.lccuellar.models.Score
import com.lccuellar.services.CoffeeShopService
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.resources.put
import io.ktor.server.resources.post
import io.ktor.server.resources.get
import io.ktor.server.resources.delete
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import services.CityService
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
@Resource("/cities")
class Cities {
    @Serializable
    @Resource("new")
    class NewCity(val parent: Cities = Cities())

    @Serializable
    @Resource("{cityID}")
    class ID(val parent: Cities = Cities(), val cityID: Int) {
        @Serializable
        @Resource("coffee_shops")
        class CityCoffeeShops(val parent: ID, val withScores: Boolean? = false)
    }
}

@Serializable
data class CityPostRequest(
    val name: String,
    val state: String,
)

@Serializable
data class CityPutRequest(
    val name: String?,
    val state: String?
)

@Serializable
data class CityResponse(
    val cityID: Int,
    val name: String,
    val state: String,
)

@Serializable
data class CoffeeShopResponse(
    val coffeeShopID: Int,
    val cityID: Int,
    val name: String,
    val date: LocalDate,
    val address: String,
    val scores: List<ScoreResponse>?
)

@Serializable
data class ScoreResponse(
    val scoreID: Int,
    val userID: Int,
    val coffeeShopID: Int,
    val scoreNum: Float,
    val scoreType: String,
    val notes: String?
)

@Serializable
data class CityWithCoffeeShopsResponse(
    val city: CityResponse,
    val coffeeShops: List<CoffeeShopResponse>
)

fun Route.cityRoutes(
    cityService: CityService,
    coffeeShopService: CoffeeShopService) {
    authenticate("auth-jwt") {
        post<Cities> {
            val request = call.receive<CityPostRequest>()
            val city = cityService.createCity(request.name, request.state)

            call.respond(toCityResponse(city))
        }
        put<Cities.ID> { city ->
            val request = call.receive<CityPutRequest>()
            val updatedCity = cityService.updateCity(city.cityID, request.name, request.state)

            if(updatedCity == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(toCityResponse(updatedCity))
            }
        }
        delete<Cities.ID> { city ->

            val deleted = cityService.deleteCity(city.cityID)
            if(deleted) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

    }
    get<Cities> {_ ->
        val allCities = cityService.getAllCities()
        val allCitiesResponse = allCities.map {
            toCityResponse(it)
        }
        call.respond(allCitiesResponse)
    }
    get<Cities.ID> { cityRequest ->
        val city = cityService.getCity(cityRequest.cityID)
        if(city != null) {
            call.respond(toCityResponse(city))
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
    get<Cities.ID.CityCoffeeShops> {cityCoffeeShopsRequest ->
        val city = cityService.getCity((cityCoffeeShopsRequest.parent.cityID))
        val queryWithScores = cityCoffeeShopsRequest.withScores ?: false
        if(city != null) {
            if(queryWithScores) {
                val coffeeShopsWithScores = coffeeShopService.getCoffeeShopsWithScoresInCity(city.id.value)
                val coffeeShopsResponse = coffeeShopsWithScores.map { toCoffeeShopResponse(it.coffeeShop, it.scores)}
                call.respond(CityWithCoffeeShopsResponse(
                    toCityResponse(city),
                    coffeeShopsResponse
                ))
            } else {
                val coffeeShops = coffeeShopService.getCoffeeShopsInCity(city.id.value)
                val coffeeShopsResponse = coffeeShops.map {toCoffeeShopResponse(it, emptyList())}
                call.respond(CityWithCoffeeShopsResponse(
                    toCityResponse(city),
                    coffeeShopsResponse
                ))
            }
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}

fun toCityResponse(city: City): CityResponse {
    return CityResponse(
        cityID = city.id.value,
        name = city.name,
        state = city.state
    )
}

fun toScoreResponse(score: Score): ScoreResponse {
    return ScoreResponse(
        scoreID = score.id.value,
        userID = score.user.id.value,
        coffeeShopID = score.coffeeShop.id.value,
        scoreNum = score.scoreNum,
        scoreType =  score.scoreType,
        notes = score.notes
    )
}

fun toCoffeeShopResponse(coffeeShop: CoffeeShop, scores: List<Score>): CoffeeShopResponse {

    return transaction {
        CoffeeShopResponse(
            coffeeShopID = coffeeShop.id.value,
            cityID = coffeeShop.city.id.value,
            name = coffeeShop.name,
            // Convert java.time.LocalDate to kotlinx.datetime.LocalDate
            date = coffeeShop.date.toKotlinLocalDate(),
            address = coffeeShop.address,
            scores = if(scores.isEmpty()) null else scores.map{ toScoreResponse(it)}
        )
    }
}
