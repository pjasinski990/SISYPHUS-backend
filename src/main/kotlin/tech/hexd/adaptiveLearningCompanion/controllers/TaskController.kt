package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskCategory
import tech.hexd.adaptiveLearningCompanion.repositories.TaskRepository
import tech.hexd.adaptiveLearningCompanion.repositories.TaskSize

@RestController
@RequestMapping("/api/tasks")
class TaskController (
    private val taskRepository: TaskRepository
) {
    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return taskRepository.findAll().let { ResponseEntity.ok(it) }
    }

    @PostMapping("/new")
    fun createTask(@RequestBody req: TaskCreateRequest, uriBuilder: UriComponentsBuilder): ResponseEntity<*> {
        val newTask = Task(category = req.category, size = req.size, description = req.description)
        val savedTask = taskRepository.save(newTask)
        val location = uriBuilder.path("/api/tasks/{id}")
            .buildAndExpand(savedTask.id)
            .toUri()

        return taskRepository.save(newTask).let { ResponseEntity.created(location).body(savedTask) }
    }
}

data class TaskCreateRequest(val category: TaskCategory, val size: TaskSize, val description: String)
