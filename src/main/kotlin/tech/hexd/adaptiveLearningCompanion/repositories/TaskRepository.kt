package tech.hexd.adaptiveLearningCompanion.repositories

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.Duration
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
    val description: String?,
    val listName: String,
    val startTime: LocalTime? = null,
    val duration: Duration? = null,
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,
    @Indexed(expireAfterSeconds = 2592000)  // 30 days
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val finishedAt: LocalDateTime? = null
)

@Repository
interface TaskRepository : MongoRepository<Task, String> {
    fun findByOwnerUsername(ownerUsername: String): List<Task>
    fun findByOwnerUsernameAndListName(ownerUsername: String, listName: String): List<Task>
}
