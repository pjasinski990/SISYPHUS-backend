package space.hexd.sisyphusBackend.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import space.hexd.sisyphusBackend.controllers.dto.TaskCreateRequest
import space.hexd.sisyphusBackend.controllers.dto.TaskDeleteResponse
import space.hexd.sisyphusBackend.controllers.dto.TaskUpdateRequest
import space.hexd.sisyphusBackend.controllers.dto.TaskUpdateResponse
import space.hexd.sisyphusBackend.repositories.TaskRepository
import space.hexd.sisyphusBackend.services.TaskService
import space.hexd.sisyphusBackend.util.ContextHelper

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

    @GetMapping("/list")
    fun getAllListTasks(authentication: Authentication, @RequestParam listName: String): ResponseEntity<*> {
        val tasks = taskService.getAllListTasksForCurrentUser(listName)
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
        val existingTask = taskRepository.findById(request.id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        if (existingTask.ownerUsername != ContextHelper.getCurrentlyLoggedUsername()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val updatedTask = request.applyTo(existingTask)
        val savedTask = taskService.updateTaskForCurrentUser(updatedTask)
        return ResponseEntity.ok().body(TaskUpdateResponse(savedTask))
    }

    @DeleteMapping("/{taskId}")
    fun deleteTask(
        authentication: Authentication,
        @PathVariable taskId: String
    ): ResponseEntity<TaskDeleteResponse> {
        return try {
            val deletedTask = taskService.deleteTaskForCurrentUser(taskId)
            ResponseEntity.ok().body(TaskDeleteResponse(deletedTask))
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
