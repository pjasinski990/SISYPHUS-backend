package tech.hexd.adaptiveLearningCompanion.controllers.dto

import tech.hexd.adaptiveLearningCompanion.repositories.DailyPlan
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import java.time.LocalDate

data class DailyPlanCreateRequest(
    val ownerUsername: String,
    val day: LocalDate,
    val todo: List<Task>,
    val done: List<Task>,
)

fun DailyPlanCreateRequest.toDailyPlan(): DailyPlan = DailyPlan(
    id = "${this.ownerUsername}:${this.day}",
    ownerUsername = this.ownerUsername,
    day = this.day,
    todo = this.todo,
    done = this.done,
)

data class DailyPlanCreateResponse(
    val plan: DailyPlan?,
)
