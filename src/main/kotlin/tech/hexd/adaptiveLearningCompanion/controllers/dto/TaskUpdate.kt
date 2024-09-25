package tech.hexd.adaptiveLearningCompanion.controllers.dto

import org.springframework.format.annotation.DateTimeFormat
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskCategory
import tech.hexd.adaptiveLearningCompanion.repositories.TaskSize
import java.security.InvalidParameterException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration

data class TaskUpdateRequest(
    val id: String,
    val category: TaskCategory,
    val size: TaskSize,
    val title: String,
    val description: String,
    val listName: String,
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    val startTime: LocalTime? = null,
    val duration: Duration? = null,
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
        )
    }

    fun applyTo(task: Task) = Task(
        id = task.id,
        ownerUsername = task.ownerUsername,
        createdAt = task.createdAt,
        category = this.category,
        size = this.size,
        title = this.title,
        description = this.description,
        listName = this.listName,
        startTime = this.startTime,
        duration = this.duration,
        updatedAt = task.updatedAt,
    )
}

data class TaskUpdateResponse(
    val task: Task?,
)
