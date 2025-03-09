package routes

import com.lccuellar.services.UserService
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


fun Route.rootRoutes() {
    get {
        call.respond(HttpStatusCode.OK)
    }
}