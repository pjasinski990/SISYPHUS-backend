package tech.hexd.adaptiveLearningCompanion.services

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import tech.hexd.adaptiveLearningCompanion.repositories.DailyPlan
import tech.hexd.adaptiveLearningCompanion.repositories.DailyPlanRepository
import java.time.LocalDate

@Service
class DailyPlanService(private val dailyPlanRepository: DailyPlanRepository) {
    fun getDailyPlanForDate(date: LocalDate): DailyPlan {
        val username = this.getCurrentlyLoggedUsername()
        var plan = dailyPlanRepository.findByOwnerUsernameAndDay(username, date)
        if (plan == null) {
            plan = this.createNewDailyPlanForUser(username, date)
        }
        return plan
    }

    private fun createNewDailyPlanForUser(username: String, date: LocalDate): DailyPlan {
        val plan = DailyPlan.newEmptyForUserAndDate(username, date)
        return dailyPlanRepository.save(plan)
    }

    private fun getCurrentlyLoggedUsername(): String {
        return SecurityContextHolder.getContext().authentication.name
    }
}