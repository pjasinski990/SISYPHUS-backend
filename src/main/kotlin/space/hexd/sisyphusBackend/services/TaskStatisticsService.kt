package space.hexd.sisyphusBackend.services

import org.springframework.stereotype.Service
import space.hexd.sisyphusBackend.repositories.TaskStatistics
import space.hexd.sisyphusBackend.repositories.TaskStatisticsRepository
import space.hexd.sisyphusBackend.util.ContextHelper
import java.time.LocalDate

@Service
class TaskStatisticsService(
    private val taskStatisticsRepository: TaskStatisticsRepository
) {
    fun getStatisticsForCurrentUserOnDate(date: LocalDate): List<TaskStatistics> {
        val username = ContextHelper.getCurrentlyLoggedUsername()
        return taskStatisticsRepository.findByOwnerUsernameAndDate(username, date)
    }

    fun getStatisticsForCurrentUserBetweenDates(startDate: LocalDate, endDate: LocalDate): List<TaskStatistics> {
        val username = ContextHelper.getCurrentlyLoggedUsername()
        return taskStatisticsRepository.findByOwnerUsernameAndDateBetween(username, startDate, endDate)
    }
}
