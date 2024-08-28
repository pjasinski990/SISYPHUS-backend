package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
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
    fun getAll(authentication: Authentication): ResponseEntity<*> {
        val username = authentication.name
        return taskRepository.findByOwnerUsername(username).let { ResponseEntity.ok(it) }
    }

    @PostMapping("/new")
    fun createTask(@RequestBody req: TaskCreateRequest, authentication: Authentication, uriBuilder: UriComponentsBuilder): ResponseEntity<*> {
        val username = authentication.name
        val newTask = Task(ownerUsername = username, category = req.category, size = req.size, description = req.description)
        val savedTask = taskRepository.save(newTask)
        val location = uriBuilder.path("/api/tasks/{id}")
            .buildAndExpand(savedTask.id)
            .toUri()

        return taskRepository.save(newTask).let { ResponseEntity.created(location).body(savedTask) }
    }
}

data class TaskCreateRequest(val category: TaskCategory, val size: TaskSize, val description: String)
