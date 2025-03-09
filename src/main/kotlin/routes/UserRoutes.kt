package com.lccuellar.routes

import com.lccuellar.models.User
import com.lccuellar.services.UserService
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
@Resource("/users")
class Users {
    @Serializable
    @Resource("register")
    class Register(val parent: Users = Users())

    @Serializable
    @Resource("login")
    class Login(val parent: Users = Users())

    @Serializable
    @Resource("{id}")
    class ID(val parent: Users = Users(), val id: Int)
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
    post<Users.Register> {
        val request = call.receive<RegisterRequest>()
        val user = userService.createUser(request.username, request.password)

        call.respond(UserResponse(user.id.value, user.username))
    }

    post<Users.Login> {
        val request = call.receive<LoginRequest>()
        val user = userService.validateUser(request.username, request.password)
        if (user != null) {
            val token = generateToken(user)
            call.respond(hashMapOf("token" to token))
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }

    authenticate("auth-jwt") {
        get<Users.ID> { _ ->
            val principal = call.principal<JWTPrincipal>()
            val userID = principal?.payload?.getClaim("user_id")?.asInt()

            if(userID != null) {
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

fun generateToken(user: User): String {
    return "Bearer ${user.id.value}"
}