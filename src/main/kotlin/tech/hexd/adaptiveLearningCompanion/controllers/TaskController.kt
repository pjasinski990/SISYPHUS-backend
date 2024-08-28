package tech.hexd.adaptiveLearningCompanion.controllers

import org.slf4j.LoggerFactory
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
        logger.info("getAll tasks request for ${authentication.name}")
        val username = authentication.name
        return taskRepository.findByOwnerUsername(username).let { ResponseEntity.ok(it) }
    }

    @PostMapping("/new")
    fun createTask(@RequestBody req: TaskCreateRequest, authentication: Authentication, uriBuilder: UriComponentsBuilder): ResponseEntity<*> {
        logger.info("new task request for ${authentication.name}")
        val username = authentication.name
        val newTask = Task(ownerUsername = username, category = req.category, size = req.size, description = req.description)
        val savedTask = taskRepository.save(newTask)
        val location = uriBuilder.path("/api/tasks/{id}")
            .buildAndExpand(savedTask.id)
            .toUri()

        return taskRepository.save(newTask).let { ResponseEntity.created(location).body(savedTask) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskController::class.java)
    }
}

data class TaskCreateRequest(val category: TaskCategory, val size: TaskSize, val description: String)
