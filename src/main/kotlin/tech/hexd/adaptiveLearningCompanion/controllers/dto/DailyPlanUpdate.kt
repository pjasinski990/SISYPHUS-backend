package tech.hexd.adaptiveLearningCompanion.controllers.dto

import tech.hexd.adaptiveLearningCompanion.repositories.DailyPlan
import tech.hexd.adaptiveLearningCompanion.repositories.Task

data class DailyPlanUpdateRequest(
    val todo: List<Task>,
    val done: List<Task>,
)

data class DailyPlanUpdateResponse(
    val plan: DailyPlan,
)
