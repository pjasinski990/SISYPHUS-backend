package tech.hexd.adaptiveLearningCompanion

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableAutoConfiguration(exclude = [SecurityAutoConfiguration::class])
@EnableMongoRepositories
class AdaptiveLearningCompanionApplication

fun main(args: Array<String>) {
	runApplication<AdaptiveLearningCompanionApplication>(*args)
}
