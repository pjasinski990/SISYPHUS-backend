package tech.hexd.adaptiveLearningCompanion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories
class AdaptiveLearningCompanionApplication

fun main(args: Array<String>) {
	runApplication<AdaptiveLearningCompanionApplication>(*args)
}
