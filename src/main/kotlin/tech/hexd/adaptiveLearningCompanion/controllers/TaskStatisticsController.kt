package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import tech.hexd.adaptiveLearningCompanion.repositories.TaskStatistics
import tech.hexd.adaptiveLearningCompanion.services.TaskStatisticsService
import java.time.LocalDate

@RestController
@RequestMapping("/api/task-statistics")
class TaskStatisticsController(
    private val taskStatisticsService: TaskStatisticsService
) {
    @GetMapping("/")
    fun getTaskStatistics(
        authentication: Authentication,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<TaskStatistics>> {
        val statistics = taskStatisticsService.getStatisticsForCurrentUserOnDate(date)
        return ResponseEntity.ok(statistics)
    }
}
