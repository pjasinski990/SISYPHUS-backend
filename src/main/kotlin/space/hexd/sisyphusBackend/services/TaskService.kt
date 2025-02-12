package space.hexd.sisyphusBackend.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import space.hexd.sisyphusBackend.controllers.UserController
import space.hexd.sisyphusBackend.repositories.TagStatistics
import space.hexd.sisyphusBackend.repositories.Task
import space.hexd.sisyphusBackend.repositories.TaskRepository
import space.hexd.sisyphusBackend.repositories.TaskStatistics
import space.hexd.sisyphusBackend.util.ContextHelper

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val mongoOperations: MongoOperations,
    private val mongoTemplate: MongoTemplate
) {
    protected val logger: Logger = LoggerFactory.getLogger(UserController::class.java)

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
        val taskWithOwner = newTask.copy(id = null, ownerUsername = username)
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

    fun deleteTaskForCurrentUser(taskId: String): Task {
        val username = ContextHelper.getCurrentlyLoggedUsername()
        val existingTask = taskRepository.findById(taskId).orElseThrow {
            NoSuchElementException("Task not found")
        }

        if (existingTask.ownerUsername != username) {
            throw AccessDeniedException("You do not have permission to delete this task")
        }
        taskRepository.delete(existingTask)
        if (existingTask.finishedAt != null) {
            decrementStatisticsForTask(existingTask)
        }

        removeTaskFromDependencies(taskId)
        return existingTask
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

        task.tags?.forEach { tag ->
            val tagQuery = Query(
                Criteria.where("date").`is`(date)
                    .and("ownerUsername").`is`(username)
                    .and("tag").`is`(tag)
            )

            val tagUpdate = Update().inc("count", 1)
            mongoOperations.upsert(tagQuery, tagUpdate, TagStatistics::class.java)
        }
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

        task.tags?.forEach { tag ->
            val tagQuery = Query(
                Criteria.where("date").`is`(date)
                    .and("ownerUsername").`is`(username)
                    .and("tag").`is`(tag)
            )

            val tagUpdate = Update().inc("count", -1)
            mongoOperations.updateFirst(tagQuery, tagUpdate, TagStatistics::class.java)
            val tagRemoveQuery = tagQuery.addCriteria(Criteria.where("count").`is`(0))
            mongoOperations.remove(tagRemoveQuery, TagStatistics::class.java)
        }
    }

    private fun removeTaskFromDependencies(taskId: String) {
        val query = Query(Criteria.where("dependencies").`is`(taskId))

        val update = Update().pull("dependencies", taskId)
        val updateResult = mongoTemplate.updateMulti(query, update, Task::class.java)
        logger.info("Updated ${updateResult.modifiedCount} tasks by removing dependency on task ID: $taskId")
    }
}
