package tech.hexd.adaptiveLearningCompanion.controllers.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskCategory
import tech.hexd.adaptiveLearningCompanion.repositories.TaskSize
import java.security.InvalidParameterException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration

data class TaskUpdateRequest(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val category: TaskCategory? = null,
    val size: TaskSize? = null,
    val listName: String? = null,
    val startTime: LocalTime? = null,
    val duration: Duration? = null,
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val finishedAt: LocalDateTime? = null,
) {
    companion object {
        fun fromTask(task: Task) = TaskUpdateRequest(
            id = task.id ?: throw InvalidParameterException("Cannot create task update request for task without ID"),
            category = task.category,
            size = task.size,
            title = task.title,
            description = task.description,
            listName = task.listName,
            startTime = task.startTime,
            duration = task.duration,
            finishedAt = task.finishedAt,
        )
    }

    fun applyTo(task: Task) = Task(
        id = task.id,
        ownerUsername = task.ownerUsername,
        createdAt = task.createdAt,
        category = this.category ?: task.category,
        size = this.size ?: task.size,
        title = this.title ?: task.title,
        description = this.description,
        listName = this.listName ?: task.listName,
        startTime = this.startTime,
        duration = this.duration,
        updatedAt = LocalDateTime.now(),
        finishedAt = this.finishedAt
    )
}

data class TaskUpdateResponse(
    val task: Task?,
)
