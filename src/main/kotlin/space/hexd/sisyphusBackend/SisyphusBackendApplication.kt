package space.hexd.sisyphusBackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories
class SisyphusBackendApplication

fun main(args: Array<String>) {
	runApplication<SisyphusBackendApplication>(*args)
}
