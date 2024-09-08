package tech.hexd.adaptiveLearningCompanion.services

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.hexd.adaptiveLearningCompanion.controllers.dto.DailyPlanUpdateRequest
import tech.hexd.adaptiveLearningCompanion.repositories.DailyPlan
import tech.hexd.adaptiveLearningCompanion.repositories.DailyPlanRepository
import java.time.LocalDate

@Service
class DailyPlanService(private val dailyPlanRepository: DailyPlanRepository) {
    @Transactional
    fun getDailyPlanForDate(date: LocalDate): DailyPlan {
        val username = this.getCurrentlyLoggedUsername()
        var plan = dailyPlanRepository.findByOwnerUsernameAndDay(username, date)
        if (plan == null) {
            LoggerFactory.getLogger("DailyPlanService").warn("Plan is null. Creating another for date $date, username: $username.")
            plan = this.createNewDailyPlanForUser(username, date)
        }
        return plan
    }

    fun updateDailyPlanForDate(date: LocalDate, plan: DailyPlanUpdateRequest): DailyPlan {
        val username = this.getCurrentlyLoggedUsername()
        val existingPlan = dailyPlanRepository.findByOwnerUsernameAndDay(username, date)
            ?: throw NoSuchElementException("No daily plan found for user $username on $date")

        val updatedPlan = existingPlan.copy(
            todo = plan.todo,
            done = plan.done
        )

        return dailyPlanRepository.save(updatedPlan)
    }

    private fun createNewDailyPlanForUser(username: String, date: LocalDate): DailyPlan {
        val plan = DailyPlan.newEmptyForUserAndDate(username, date)
        return dailyPlanRepository.save(plan)
    }

    private fun getCurrentlyLoggedUsername(): String {
        return SecurityContextHolder.getContext().authentication.name
    }
}
