package space.hexd.sisyphusBackend.controllers.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.format.annotation.DateTimeFormat
import space.hexd.sisyphusBackend.repositories.Task
import space.hexd.sisyphusBackend.repositories.TaskCategory
import space.hexd.sisyphusBackend.repositories.TaskSize
import space.hexd.sisyphusBackend.util.ContextHelper
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration

data class TaskCreateRequest(
    val category: TaskCategory,
    val size: TaskSize,
    val title: String,
    val description: String?,
    val listName: String,
    val startTime: LocalTime? = null,
    val duration: Duration? = null,
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val deadline: LocalDateTime? = null,
    val tags: List<String>? = null,
    val dependencies: List<String>? = null,
    val flexibility: Float? = null,
) {
    companion object {
        fun fromTask(task: Task) = TaskCreateRequest(
            category = task.category,
            size = task.size,
            title = task.title,
            description = task.description,
            listName = task.listName,
            startTime = task.startTime,
            duration = task.duration,
            deadline = task.deadline,
            tags = task.tags,
            dependencies = task.dependencies,
            flexibility = task.flexibility,
        )
    }

    fun toTask() = Task(
        ownerUsername = ContextHelper.getCurrentlyLoggedUsername(),
        category = this.category,
        size = this.size,
        title = this.title,
        description = this.description,
        listName = this.listName,
        startTime = this.startTime,
        duration = this.duration,
        deadline = this.deadline,
        tags = this.tags,
        dependencies = this.dependencies,
        flexibility = this.flexibility,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
    )
}

data class TaskCreateResponse(
    val task: Task?,
)
