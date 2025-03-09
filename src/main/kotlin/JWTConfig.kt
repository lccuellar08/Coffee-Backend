// JWTConfig.kt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWTVerifier
import java.util.Date

object JWTConfig {
    private const val SECRET = "your-secret-key" // In production, use environment variable
    private const val ISSUER = "your-app"
    private const val AUDIENCE = "your-app-users"
    const val REALM = "Protected API"
    private const val EXPIRATION_TIME = 3600000 // 1 hour in milliseconds

    private val algorithm = Algorithm.HMAC256(SECRET)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(userId: Int, username: String): String = JWT.create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim("user_id", userId)
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .sign(algorithm)
}