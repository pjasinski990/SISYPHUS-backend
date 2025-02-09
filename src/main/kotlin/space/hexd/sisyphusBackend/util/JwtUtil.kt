package space.hexd.sisyphusBackend.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtUtil {
    private final val fifteenMinutesMs = 1000 * 60 * 15
    private val expirationMs = fifteenMinutesMs
    private val secret = Keys.secretKeyFor(SignatureAlgorithm.HS256)

    fun generateToken(username: String, expiration: Int = expirationMs): String {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .claim("roles", listOf("ROLE_USER"))
            .signWith(secret)
            .compact()
    }

    fun generateAdminToken(username: String, expiration: Int = expirationMs): String {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .claim("roles", listOf("ROLE_ADMIN"))
            .signWith(secret)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun extractUsername(token: String): String {
        return Jwts.parserBuilder().setSigningKey(secret).build()
            .parseClaimsJws(token).body.subject
    }

    fun extractRoles(token: String): List<String> {
        val claims = Jwts.parserBuilder()
            .setSigningKey(secret)
            .build()
            .parseClaimsJws(token)
            .body

        return when (val rolesObject = claims["roles"]) {
            is List<*> -> rolesObject.filterIsInstance<String>()
            is String -> listOf(rolesObject)
            else -> emptyList()
        }
    }
}
