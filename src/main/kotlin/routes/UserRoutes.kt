package com.lccuellar.routes

import JWTConfig.generateToken
import com.lccuellar.services.UserService
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
//import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.resources.post
import io.ktor.server.resources.get
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/users")
class UserRoutes {
    @Serializable
    @Resource("login")
    class Login(val parent: UserRoutes = UserRoutes())

    @Serializable
    @Resource("register")
    class Register(val parent: UserRoutes = UserRoutes())

    @Serializable
    @Resource("{userID}")
    class ID(val parent: UserRoutes = UserRoutes(), val userID: Int)
}

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class UserResponse(
    val id: Int,
    val username: String,
)

fun Route.userRoutes(userService: UserService) {
    post<UserRoutes.Register> {
        val request = call.receive<RegisterRequest>()
        val user = userService.createUser(request.username, request.password)

        call.respond(UserResponse(user.id.value, user.username))
    }

    post<UserRoutes.Login> {
        println("Hit users login")
        val request = call.receive<LoginRequest>()
        val user = userService.validateUser(request.username, request.password)
        if (user != null) {
            val token = generateToken(user.id.value, user.username)
            call.respond(hashMapOf("token" to token))
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }

    authenticate("auth-jwt") {
        get<UserRoutes.ID> { userReq ->
            println("Hit users id get")
            val userID = userReq.userID
            val principal = call.principal<JWTPrincipal>()
            val thisUserID = principal?.payload?.getClaim("user_id")?.asInt()

            if(thisUserID != null) {
                val user = userService.findByID(userID)
                if(user != null) {
                    call.respond(UserResponse(user.id.value, user.username))
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}