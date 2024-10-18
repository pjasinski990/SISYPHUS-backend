package tech.hexd.adaptiveLearningCompanion.repositories

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Document(collection = "tagStatistics")
data class TagStatistics(
    @Id
    val id: String? = null,
    val date: LocalDate,
    val ownerUsername: String,
    val tag: String,
    val count: Int = 0
)

@Repository
interface TagStatisticsRepository : MongoRepository<TagStatistics, String> {
    fun findByOwnerUsernameAndDate(ownerUsername: String, date: LocalDate): List<TagStatistics>
    fun findByOwnerUsernameAndDateBetween(ownerUsername: String, startDate: LocalDate, endDate: LocalDate): List<TagStatistics>
}
