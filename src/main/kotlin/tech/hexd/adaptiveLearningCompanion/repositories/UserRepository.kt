package tech.hexd.adaptiveLearningCompanion.repositories

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "users")
data class User (
    @Id
    val id: String? = null,
    val username: String,
    val password: String
)

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByUsername(username: String): User?
}
