package tech.hexd.adaptiveLearningCompanion.controllers.dto

import org.springframework.format.annotation.DateTimeFormat
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskCategory
import tech.hexd.adaptiveLearningCompanion.repositories.TaskSize
import tech.hexd.adaptiveLearningCompanion.util.ContextHelper
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration

data class TaskCreateRequest(
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
        fun fromTask(task: Task) = TaskCreateRequest(
            category = task.category,
            size = task.size,
            title = task.title,
            description = task.description,
            listName = task.listName,
            startTime = task.startTime,
            duration = task.duration,
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
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
    )
}

data class TaskCreateResponse(
    val task: Task?,
)
