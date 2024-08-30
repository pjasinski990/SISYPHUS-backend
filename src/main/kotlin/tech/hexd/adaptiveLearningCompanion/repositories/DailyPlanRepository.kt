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
    val category: TaskCategory,
    val size: TaskSize,
    val description: String,
)

@Repository
interface DailyPlanRepository : MongoRepository<DailyPlan, String> {
    fun findByOwnerUsername(ownerUsername: String): List<DailyPlan>
    fun findByOwnerUsernameAndDay(ownerUsername: String, day: LocalDate): List<DailyPlan>
}
