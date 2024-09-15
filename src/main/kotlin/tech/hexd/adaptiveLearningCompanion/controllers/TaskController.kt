package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import tech.hexd.adaptiveLearningCompanion.controllers.dto.*
import tech.hexd.adaptiveLearningCompanion.repositories.TaskRepository
import tech.hexd.adaptiveLearningCompanion.services.TaskService
import tech.hexd.adaptiveLearningCompanion.util.ContextHelper

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

    @PutMapping("/")
    fun updateTask(
        authentication: Authentication,
        @RequestBody request: TaskUpdateRequest
    ): ResponseEntity<TaskUpdateResponse> {
        val taskId = request.id
        val existingTask = taskRepository.findById(taskId).orElse(null)
            ?: return ResponseEntity.notFound().build()

        if (existingTask.ownerUsername != ContextHelper.getCurrentlyLoggedUsername()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val updatedTask = request.applyTo(existingTask)
        val savedTask = taskRepository.save(updatedTask)
        return ResponseEntity.ok().body(TaskUpdateResponse(savedTask))
    }

    @DeleteMapping("/{taskId}")
    fun deleteTask(
        authentication: Authentication,
        @PathVariable taskId: String
    ): ResponseEntity<TaskDeleteResponse> {
        val deletedTask = taskRepository.findById(taskId).orElse(null)
            ?: return ResponseEntity.notFound().build()

        if (deletedTask.ownerUsername != ContextHelper.getCurrentlyLoggedUsername()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        taskRepository.delete(deletedTask)
        return ResponseEntity.ok().body(TaskDeleteResponse(deletedTask))
    }
}
