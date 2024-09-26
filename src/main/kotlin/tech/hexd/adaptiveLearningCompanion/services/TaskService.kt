package tech.hexd.adaptiveLearningCompanion.services

import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskRepository
import tech.hexd.adaptiveLearningCompanion.repositories.TaskStatistics
import tech.hexd.adaptiveLearningCompanion.util.ContextHelper

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val mongoOperations: MongoOperations
) {
    fun getAllTasksForCurrentUser(): List<Task> {
        val username = ContextHelper.getCurrentlyLoggedUsername()
        return taskRepository.findByOwnerUsername(username)
    }

    fun getAllListTasksForCurrentUser(listName: String): List<Task> {
        val username = ContextHelper.getCurrentlyLoggedUsername()
        return taskRepository.findByOwnerUsernameAndListName(username, listName)
    }

    fun createNewTaskForCurrentUser(newTask: Task): Task {
        val username = ContextHelper.getCurrentlyLoggedUsername()
        val taskWithOwner = newTask.copy(ownerUsername = username)
        return taskRepository.save(taskWithOwner)
    }

    fun updateTaskForCurrentUser(updatedTask: Task): Task {
        val username = ContextHelper.getCurrentlyLoggedUsername()
        val existingTask = taskRepository.findById(updatedTask.id!!).orElseThrow {
            IllegalArgumentException("Task not found")
        }

        if (existingTask.ownerUsername != username) {
            throw IllegalAccessException("You do not have permission to update this task")
        }

        val wasCompleted = existingTask.finishedAt != null
        val isNowCompleted = updatedTask.finishedAt != null

        val savedTask = taskRepository.save(updatedTask)

        when {
            wasCompleted && !isNowCompleted -> {
                decrementStatisticsForTask(existingTask)
            }
            !wasCompleted && isNowCompleted -> {
                incrementStatisticsForTask(savedTask)
            }
            wasCompleted && isNowCompleted -> {
                if (qualifiersOrDateChanged(existingTask, updatedTask)) {
                    decrementStatisticsForTask(existingTask)
                    incrementStatisticsForTask(savedTask)
                }
            }
        }
        return savedTask
    }

    private fun qualifiersOrDateChanged(oldTask: Task, newTask: Task): Boolean {
        return oldTask.category != newTask.category ||
                oldTask.size != newTask.size ||
                oldTask.finishedAt?.toLocalDate() != newTask.finishedAt?.toLocalDate()
    }

    private fun incrementStatisticsForTask(task: Task) {
        val date = task.finishedAt!!.toLocalDate()
        val username = task.ownerUsername

        val query = Query(
            Criteria.where("date").`is`(date)
                .and("ownerUsername").`is`(username)
                .and("category").`is`(task.category)
                .and("size").`is`(task.size)
        )

        val update = Update().inc("count", 1)
        mongoOperations.upsert(query, update, TaskStatistics::class.java)
    }

    private fun decrementStatisticsForTask(task: Task) {
        val date = task.finishedAt!!.toLocalDate()
        val username = task.ownerUsername

        val query = Query(
            Criteria.where("date").`is`(date)
                .and("ownerUsername").`is`(username)
                .and("category").`is`(task.category)
                .and("size").`is`(task.size)
        )

        val update = Update().inc("count", -1)
        mongoOperations.updateFirst(query, update, TaskStatistics::class.java)
        val removeQuery = query.addCriteria(Criteria.where("count").`is`(0))
        mongoOperations.remove(removeQuery, TaskStatistics::class.java)
    }
}
