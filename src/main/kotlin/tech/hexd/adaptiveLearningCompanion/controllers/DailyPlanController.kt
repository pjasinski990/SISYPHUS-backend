package tech.hexd.adaptiveLearningCompanion.controllers

import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import tech.hexd.adaptiveLearningCompanion.repositories.DailyPlan
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.services.DailyPlanService
import tech.hexd.adaptiveLearningCompanion.util.ResponseForger
import java.time.LocalDate

@RestController
@RequestMapping("/daily-plan")
class DailyPlanController(private val dailyPlanService: DailyPlanService) {
    @GetMapping("/{date}")
    fun getDailyPlanForDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<Map<String, Any>> {
        val plan = dailyPlanService.getDailyPlanForDate(date)
        return ResponseForger().ok().withField("plan", plan).build()
    }

    @PutMapping("/{date}")
    fun updateDailyPlanForDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestBody @Valid request: DailyPlanCreateRequest,
        authentication: Authentication,
        uriBuilder: UriComponentsBuilder,
    ): ResponseEntity<Map<String, Any>> {
        if (authentication.name != request.ownerUsername) {
            return ResponseEntity.badRequest().build()
        }

        return try {
            val updatedDailyPlan = dailyPlanService.updateDailyPlanForDate(date, request.toDailyPlan())

            val location = uriBuilder.path("/api/daily-plans/{id}")
                .buildAndExpand(updatedDailyPlan.id)
                .toUri()
            ResponseEntity.status(HttpStatusCode.valueOf(204)).location(location).build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}

data class DailyPlanCreateRequest(
    val ownerUsername: String,
    val day: LocalDate,
    val todo: List<Task>,
    val done: List<Task>,
)

fun DailyPlanCreateRequest.toDailyPlan(): DailyPlan = DailyPlan(
    ownerUsername = this.ownerUsername,
    day = this.day,
    todo = this.todo,
    done = this.done,
)
