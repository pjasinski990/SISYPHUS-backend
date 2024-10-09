package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.hexd.adaptiveLearningCompanion.controllers.dto.TaskUnravelContext
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.services.GenerativeService

@RestController
@RequestMapping("/generate")
class GenerativeController(private val generativeService: GenerativeService) {

    @PostMapping("/unravel/task")
    fun unravelTask(@RequestBody context: TaskUnravelContext): ResponseEntity<List<Task>> {
        return generativeService.unravel(context);
    }
}
