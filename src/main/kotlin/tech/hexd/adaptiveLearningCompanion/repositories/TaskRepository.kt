package tech.hexd.adaptiveLearningCompanion.repositories

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.LocalTime

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
    val ownerUsername: String,
    val category: TaskCategory,
    val size: TaskSize,
    val title: String,
    val description: String,
    val reusable: Boolean,
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,
    val startTime: LocalTime? = null,
)

@Repository
interface TaskRepository : MongoRepository<Task, String> {
    fun findByOwnerUsername(ownerUsername: String): List<Task>
}
