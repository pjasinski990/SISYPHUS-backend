package tech.hexd.adaptiveLearningCompanion.controllers.dto

import org.springframework.format.annotation.DateTimeFormat
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskCategory
import tech.hexd.adaptiveLearningCompanion.repositories.TaskSize
import tech.hexd.adaptiveLearningCompanion.util.ContextHelper
import java.time.LocalDateTime
import java.time.LocalTime

data class TaskCreateRequest(
    val category: TaskCategory,
    val size: TaskSize,
    val title: String,
    val description: String,
    val reusable: Boolean = false,
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    val startTime: LocalTime? = null,
) {
    companion object {
        fun fromTask(task: Task) = TaskCreateRequest(
            category = task.category,
            size = task.size,
            title = task.title,
            description = task.description,
            reusable = task.reusable,
        )
    }

    fun toTask() = Task(
        ownerUsername = ContextHelper.getCurrentlyLoggedUsername(),
        category = this.category,
        size = this.size,
        title = this.title,
        description = this.description,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        reusable = this.reusable,
    )

}

data class TaskCreateResponse(
    val task: Task?,
)
