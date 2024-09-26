package tech.hexd.adaptiveLearningCompanion.services

import org.springframework.stereotype.Service
import tech.hexd.adaptiveLearningCompanion.repositories.TaskStatistics
import tech.hexd.adaptiveLearningCompanion.repositories.TaskStatisticsRepository
import tech.hexd.adaptiveLearningCompanion.util.ContextHelper
import java.time.LocalDate

@Service
class TaskStatisticsService(
    private val taskStatisticsRepository: TaskStatisticsRepository
) {
    fun getStatisticsForCurrentUserOnDate(date: LocalDate): List<TaskStatistics> {
        val username = ContextHelper.getCurrentlyLoggedUsername()
        return taskStatisticsRepository.findByOwnerUsernameAndDate(username, date)
    }
}
