package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import tech.hexd.adaptiveLearningCompanion.dependencies.OpenAIService
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.services.GenerativeService

@RestController
class GenerativeController(private val generativeService: GenerativeService) {

    @Autowired
    private lateinit var openAIService: OpenAIService

    @GetMapping("/task/{taskId}/unravel")
    fun unravelTask(@PathVariable taskId: String): ResponseEntity<List<Task>> {

        return ResponseEntity.ok(emptyList())
    }
}
