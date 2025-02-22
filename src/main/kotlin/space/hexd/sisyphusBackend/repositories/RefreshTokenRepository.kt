package space.hexd.sisyphusBackend.repositories

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import space.hexd.sisyphusBackend.repositories.AppUser
import java.time.LocalDateTime

@Document(collection = "refresh_tokens")
data class RefreshToken(
    @Id
    val id: String? = null,
    val token: String,
    @DBRef
    val user: AppUser,
    val expiryDateTime: LocalDateTime,
    val deviceInfo: String? = null,
    val ipAddress: String? = null,
)
@Repository
interface RefreshTokenRepository : MongoRepository<RefreshToken, String> {
    fun findByToken(token: String): RefreshToken?
}
