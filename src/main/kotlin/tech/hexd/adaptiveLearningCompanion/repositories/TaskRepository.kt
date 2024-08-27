package tech.hexd.adaptiveLearningCompanion.repositories

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

enum class TaskCategory {
    GREEN,
    BLUE,
    RED,
    YELLOW,
    WHITE,
    PINK,
}

enum class TaskSize {
    SMALL,
    BIG,
}

@Document(collection = "tasks")
data class Task (
    @Id
    val id: String? = null,
    val category: TaskCategory,
    val size: TaskSize,
    val description: String,
    val userId: String? = null,
)

@Repository
interface TaskRepository : MongoRepository<Task, String> {
    fun findByUserId(userId: String): List<Task>
}
