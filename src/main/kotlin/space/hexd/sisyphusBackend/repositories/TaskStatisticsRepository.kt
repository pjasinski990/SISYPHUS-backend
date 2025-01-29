package space.hexd.sisyphusBackend.repositories

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Document(collection = "taskStatistics")
data class TaskStatistics(
    @Id
    val id: String? = null,
    val date: LocalDate,
    val ownerUsername: String,
    val category: TaskCategory,
    val size: TaskSize,
    val count: Int = 0
)

@Repository
interface TaskStatisticsRepository : MongoRepository<TaskStatistics, String> {
    fun findByOwnerUsernameAndDate(ownerUsername: String, date: LocalDate): List<TaskStatistics>
    fun findByOwnerUsernameAndDateBetween(username: String, startDate: LocalDate, endDate: LocalDate): List<TaskStatistics>
}
