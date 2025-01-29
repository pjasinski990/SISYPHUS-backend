package space.hexd.sisyphusBackend.controllers.dto

import space.hexd.sisyphusBackend.repositories.Task

data class TaskDeleteRequest(val taskId: String)

data class TaskDeleteResponse(val task: Task?)
