package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskCategory
import tech.hexd.adaptiveLearningCompanion.repositories.TaskSize
import tech.hexd.adaptiveLearningCompanion.services.TaskService
import java.time.LocalTime

@RestController
@RequestMapping("/api/tasks")
class TaskController (
    @Autowired val taskService: TaskService,
) {
    @GetMapping("/")
    fun getAllTasks(authentication: Authentication): ResponseEntity<*> {
        val tasks = taskService.getAllTasksForCurrentUser()
        return tasks.let { ResponseEntity.ok(it) }
    }

    @PostMapping("/new")
    fun createNewTask(@RequestBody req: TaskCreateRequest, authentication: Authentication, uriBuilder: UriComponentsBuilder): ResponseEntity<*> {
        val savedTask = taskService.createNewTaskForCurrentUser(req.toTask());

        val location = uriBuilder.path("/api/tasks/{id}")
            .buildAndExpand(savedTask.id)
            .toUri()
        return ResponseEntity.created(location).body(savedTask)
    }
}

data class TaskCreateRequest(
    val category: TaskCategory,
    val size: TaskSize,
    val description: String,
    val startTime: LocalTime? = null
)

fun TaskCreateRequest.toTask(): Task {
    return Task(
        category = this.category,
        size = this.size,
        description = this.description,
    )
}
