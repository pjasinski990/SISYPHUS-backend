package tech.hexd.adaptiveLearningCompanion.controllers.dto

import org.springframework.format.annotation.DateTimeFormat
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskCategory
import tech.hexd.adaptiveLearningCompanion.repositories.TaskSize
import java.security.InvalidParameterException
import java.time.LocalDateTime
import java.time.LocalTime

data class TaskUpdateRequest(
    val id: String,
    val category: TaskCategory,
    val size: TaskSize,
    val title: String,
    val description: String,
    val reusable: Boolean = false,
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    val startTime: LocalTime? = null,
) {
    companion object {
        fun fromTask(task: Task) = TaskUpdateRequest(
            id = task.id ?: throw InvalidParameterException("Cannot create task update request for task without ID"),
            category = task.category,
            size = task.size,
            title = task.title,
            description = task.description,
            reusable = task.reusable,
            startTime = task.startTime,
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
        reusable = this.reusable,
        startTime = this.startTime,
        updatedAt = LocalDateTime.now(),
    )
}

data class TaskUpdateResponse(
    val task: Task?,
)
