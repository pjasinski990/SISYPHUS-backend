package tech.hexd.adaptiveLearningCompanion.controllers

import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import tech.hexd.adaptiveLearningCompanion.controllers.dto.DailyPlanCreateResponse
import tech.hexd.adaptiveLearningCompanion.controllers.dto.DailyPlanUpdateRequest
import tech.hexd.adaptiveLearningCompanion.controllers.dto.DailyPlanUpdateResponse
import tech.hexd.adaptiveLearningCompanion.services.DailyPlanService
import java.time.LocalDate

@RestController
@RequestMapping("/daily-plan")
class DailyPlanController(private val dailyPlanService: DailyPlanService) {
    @GetMapping("/{date}")
    fun getDailyPlanForDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<DailyPlanCreateResponse> {
        return let {
            val plan = dailyPlanService.getDailyPlanForDate(date)
            ResponseEntity.ok().body(DailyPlanCreateResponse(plan))
        }
    }

    @PutMapping("/{date}")
    fun updateDailyPlanForDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestBody @Valid request: DailyPlanUpdateRequest,
        authentication: Authentication,
    ): ResponseEntity<DailyPlanUpdateResponse> {
        return try {
            val updatedDailyPlan = dailyPlanService.updateDailyPlanForDate(date, request)
            ResponseEntity.status(HttpStatusCode.valueOf(204)).body(DailyPlanUpdateResponse(updatedDailyPlan))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}
