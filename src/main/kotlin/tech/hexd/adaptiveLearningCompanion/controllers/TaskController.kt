package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import tech.hexd.adaptiveLearningCompanion.controllers.dto.TaskCreateRequest
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskRepository
import tech.hexd.adaptiveLearningCompanion.services.TaskService
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    @Autowired val taskService: TaskService,
    private val taskRepository: TaskRepository,
) {
    @GetMapping("/")
    fun getAllTasks(authentication: Authentication): ResponseEntity<*> {
        val tasks = taskService.getAllTasksForCurrentUser()
        return ResponseEntity.ok(tasks)
    }

    @PostMapping("/")
    fun createNewTask(@RequestBody req: TaskCreateRequest, authentication: Authentication, uriBuilder: UriComponentsBuilder): ResponseEntity<*> {
        val savedTask = taskService.createNewTaskForCurrentUser(req.toTask());

        val location = uriBuilder.path("/api/tasks/{id}")
            .buildAndExpand(savedTask.id)
            .toUri()
        return ResponseEntity.created(location).body(savedTask)
    }

    @PutMapping("/{taskId}")
    fun updateTask(
        authentication: Authentication,
        @PathVariable taskId: String,
        @RequestBody task: Task
    ): ResponseEntity<Task> {
        val existingTask = taskRepository.findById(taskId).orElse(null)
            ?: return ResponseEntity.notFound().build()

        if (task.id != taskId) {
            return ResponseEntity.badRequest().build()
        }

        val updatedTask = existingTask.copy(
            category = task.category,
            size = task.size,
            title = task.title,
            description = task.description,
            updatedAt = LocalDateTime.now(),
            reusable = task.reusable,
        )

        val savedTask = taskRepository.save(updatedTask)
        return ResponseEntity.ok(savedTask)
    }
}
