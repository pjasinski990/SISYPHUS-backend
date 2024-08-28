package tech.hexd.adaptiveLearningCompanion.repositories

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "users")
data class AppUser (
    @Id
    val id: String? = null,
    val username: String,
    val password: String,
    val roles: List<String>,
)

@Repository
interface AppUserRepository : MongoRepository<AppUser, String> {
    fun findByUsername(username: String): AppUser?
}
