package routes

import com.lccuellar.models.CoffeeShop
import com.lccuellar.models.Score
import com.lccuellar.services.CoffeeShopService
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
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
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.transactions.transaction
import utils.LocalDateSerializer

@Serializable
@Resource("/coffeeshops")
class CoffeeShopRoutes {
    @Serializable
    @Resource("{coffeeShopID}")
    class CoffeeShopIDRoute(val parent: CoffeeShopRoutes = CoffeeShopRoutes(), val coffeeShopID: Int) {
        @Serializable
        @Resource("scores")
        class Scores(val parent: CoffeeShopIDRoute) {
            @Serializable
            @Resource("{scoreID}")
            class ScoreIDRoute(val parent: Scores, val scoreID: Int)
        }
    }
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
data class ScorePostRequest(
    val scoreNum: Float,
    val scoreType: String,
    val notes: String?
)

@Serializable
data class ScorePatchRequest(
    val scoreNum: Float?,
    val scoreType: String?,
    val notes: String?
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

        patch<CoffeeShopRoutes.CoffeeShopIDRoute> { coffeeShop ->
            val request = call.receive<CoffeeShopPatchRequest>()
            val updatedCoffeeShop = coffeeShopService.updateCoffeeShop(coffeeShop.coffeeShopID, request.name, request.date, request.address)

            if(updatedCoffeeShop == null) {
                call.respond(HttpStatusCode.NotFound)
                return@patch
            }
            call.respond(toCoffeeShopResponse(updatedCoffeeShop, emptyList()))
        }

        delete<CoffeeShopRoutes.CoffeeShopIDRoute> { coffeeShop ->
            val deleted = coffeeShopService.deleteCoffeeShop(coffeeShop.coffeeShopID)
            if(deleted) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Conflict)
            }
        }
    }

    get<CoffeeShopRoutes.CoffeeShopIDRoute> { coffeeShopRequest ->
        val coffeeShopWithScores = coffeeShopService.getCoffeeShopWithScores(coffeeShopRequest.coffeeShopID)
        if(coffeeShopWithScores == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }
        val coffeeShopResponse = toCoffeeShopResponse(coffeeShopWithScores.coffeeShop, coffeeShopWithScores.scores)
        call.respond(coffeeShopResponse)
    }

    get<CoffeeShopRoutes.CoffeeShopIDRoute.Scores> { scores ->
        val coffeeShopScores = coffeeShopService.getCoffeeShopScores(scores.parent.coffeeShopID)
        val coffeeShopScoresResponse = coffeeShopScores.map{ toScoreResponse(it)}
        call.respond(coffeeShopScoresResponse)
    }

    authenticate("auth-jwt") {
        post<CoffeeShopRoutes.CoffeeShopIDRoute.Scores> { score ->
            val request = call.receive<ScorePostRequest>()
            val principal = call.principal<JWTPrincipal>()
            val userID = principal?.payload?.getClaim("user_id")?.asInt()

            if(userID == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
                return@post
            }

            val newScore = coffeeShopService.createCoffeeShopScore(score.parent.coffeeShopID, userID, request.scoreNum, request.scoreType, request.notes)
            call.respond(toScoreResponse(newScore))
        }

        patch<CoffeeShopRoutes.CoffeeShopIDRoute.Scores.ScoreIDRoute> {score ->
            val request = call.receive<ScorePatchRequest>()
            val updatedScore = coffeeShopService.updateCoffeeShopScore(score.scoreID, request.scoreNum, request.scoreType, request.notes)
            if(updatedScore == null) {
                call.respond(HttpStatusCode.NotFound, "Score not found")
                return@patch
            }

            call.respond(toScoreResponse(updatedScore))
        }

        delete<CoffeeShopRoutes.CoffeeShopIDRoute.Scores.ScoreIDRoute> {score ->
            val deleted = coffeeShopService.deleteCoffeeShopScore(score.scoreID)
            if(deleted) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Conflict)
            }
        }
    }
}

fun toScoreResponse(score: Score): ScoreResponse {
    return transaction {
        ScoreResponse(
            scoreID = score.id.value,
            userID = score.user.id.value,
            coffeeShopID = score.coffeeShop.id.value,
            scoreNum = score.scoreNum,
            scoreType =  score.scoreType,
            notes = score.notes
        )
    }
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