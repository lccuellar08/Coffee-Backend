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
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.resources.get
import io.ktor.server.resources.delete
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import services.CityService
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.transactions.transaction
import utils.LocalDateSerializer

@Serializable
@Resource("/coffeeshops")
class CoffeeShopRoutes {
    @Serializable
    @Resource("{coffeeShopID}")
    class ID(val parent: CoffeeShopRoutes = CoffeeShopRoutes(), val coffeeShopID: Int)
}

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
data class CoffeeShopPostRequest(
    val cityID: Int,
    val name: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: java.time.LocalDate,
    val address: String
)

@Serializable
data class CoffeeShopPatchRequest(
    val name: String?,
    @Serializable(with = LocalDateSerializer::class)
    val date: java.time.LocalDate?,
    val address: String?
)

fun Route.coffeeShopRoutes(
    coffeeShopService: CoffeeShopService,
    cityService: CityService
) {
    authenticate("auth-jwt") {
        post<CoffeeShopRoutes> {
            val request = call.receive<CoffeeShopPostRequest>()
            val city = cityService.getCity(request.cityID)
            if(city == null) {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            val newCoffeeShop = coffeeShopService.createCoffeeShop(
                city.id.value,
                request.name,
                request.date,
                request.address
            )
            if(newCoffeeShop == null) {
                call.respond(HttpStatusCode.BadRequest, "Unable to create coffee shop")
                return@post
            }

            call.respond(toCoffeeShopResponse(newCoffeeShop, emptyList()))
        }

        patch<CoffeeShopRoutes.ID> { coffeeShop ->
            val request = call.receive<CoffeeShopPatchRequest>()
            val updatedCoffeeShop = coffeeShopService.updateCoffeeShop(coffeeShop.coffeeShopID, request.name, request.date, request.address)

            if(updatedCoffeeShop == null) {
                call.respond(HttpStatusCode.NotFound)
                return@patch
            }
            call.respond(toCoffeeShopResponse(updatedCoffeeShop, emptyList()))
        }

        delete<CoffeeShopRoutes.ID> { coffeeShop ->
            val deleted = coffeeShopService.deleteCoffeeShop(coffeeShop.coffeeShopID)
            if(deleted) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Conflict)
            }
        }
    }
    get<CoffeeShopRoutes.ID> {coffeeShopRequest ->
        val coffeeShopWithScores = coffeeShopService.getCoffeeShopWithScores(coffeeShopRequest.coffeeShopID)
        if(coffeeShopWithScores == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }
        val coffeeShopResponse = toCoffeeShopResponse(coffeeShopWithScores.coffeeShop, coffeeShopWithScores.scores)
        call.respond(coffeeShopResponse)
    }
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