package tech.hexd.adaptiveLearningCompanion.controllers.dto

data class TaskUnravelContext(
    val taskId: String,
    val nTasks: Int,
    val additionalContext: String,
)
