package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
}
