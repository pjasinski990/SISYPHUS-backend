package tech.hexd.adaptiveLearningCompanion.repositories

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Document(collection = "daily_plan")
data class DailyPlan (
    @Id
    val id: String? = null,
    val ownerUsername: String? = null,
    val day: LocalDate,
    val todo: List<Task>,
    val done: List<Task>,
) {
    companion object {
        fun newEmptyForUserAndDate(username: String, date: LocalDate): DailyPlan {
            return DailyPlan(
                ownerUsername = username,
                day = date,
                todo = emptyList(),
                done = emptyList(),
            )
        }
    }
}

@Repository
interface DailyPlanRepository : MongoRepository<DailyPlan, String> {
    fun findByOwnerUsername(ownerUsername: String): List<DailyPlan>
    fun findByOwnerUsernameAndDay(ownerUsername: String, day: LocalDate): DailyPlan?
}
