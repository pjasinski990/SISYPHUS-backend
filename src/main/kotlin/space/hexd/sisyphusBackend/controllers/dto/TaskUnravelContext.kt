package space.hexd.sisyphusBackend.controllers.dto

data class TaskUnravelContext(
    val taskId: String,
    val additionalContext: String,
)
