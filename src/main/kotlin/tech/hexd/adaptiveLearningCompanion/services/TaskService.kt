package tech.hexd.adaptiveLearningCompanion.services

import org.springframework.stereotype.Service
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskRepository
import tech.hexd.adaptiveLearningCompanion.util.ContextHelper

@Service
class TaskService (
    private val taskRepository: TaskRepository,
) {
    fun getAllTasksForCurrentUser(): List<Task> {
        val username = ContextHelper.getCurrentlyLoggedUsername()
        return taskRepository.findByOwnerUsername(username)
    }

    fun createNewTaskForCurrentUser(newTask: Task): Task {
        val username = ContextHelper.getCurrentlyLoggedUsername()
        val taskWithOwner = newTask.copy(ownerUsername = username)
        return taskRepository.save(taskWithOwner)
    }
}