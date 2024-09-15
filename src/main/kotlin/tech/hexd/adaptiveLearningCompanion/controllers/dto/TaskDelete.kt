package tech.hexd.adaptiveLearningCompanion.controllers.dto

import tech.hexd.adaptiveLearningCompanion.repositories.Task

data class TaskDeleteRequest(val taskId: String)

data class TaskDeleteResponse(val task: Task?)
