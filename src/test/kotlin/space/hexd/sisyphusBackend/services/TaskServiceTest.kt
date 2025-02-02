package space.hexd.sisyphusBackend.services

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import space.hexd.sisyphusBackend.repositories.*
import space.hexd.sisyphusBackend.util.ContextHelper
import java.time.LocalDateTime
import java.util.*

class TaskServiceTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskStatisticsRepository: TaskStatisticsRepository
    private lateinit var mongoOperations: MongoOperations
    private lateinit var mongoTemplate: MongoTemplate
    private lateinit var taskService: TaskService

    @BeforeEach
    fun setup() {
        taskRepository = mockk(relaxed = true)
        taskStatisticsRepository = mockk(relaxed = true)
        mongoOperations = mockk(relaxed = true)
        mongoTemplate = mockk(relaxed = true)
        taskService = TaskService(taskRepository, mongoOperations, mongoTemplate)

        mockkObject(ContextHelper.Companion)
        every { ContextHelper.getCurrentlyLoggedUsername() } returns "testUser"
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(ContextHelper.Companion)
        clearAllMocks()
    }

    @Test
    fun `should increment statistics when task is marked as completed`() {
        val taskId = "taskId1"
        val existingTask = Task(
            id = taskId,
            ownerUsername = "testUser",
            category = TaskCategory.BLUE,
            size = TaskSize.SMALL,
            title = "Test Task",
            description = "Test Description",
            listName = "ToDo",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            finishedAt = null
        )
        val updatedTask = existingTask.copy(finishedAt = LocalDateTime.now())

        every { taskRepository.findById(taskId) } returns Optional.of(existingTask)
        every { taskRepository.save(updatedTask) } returns updatedTask

        every {
            mongoOperations.upsert(any<Query>(), any<Update>(), TaskStatistics::class.java)
        } returns mockk()

        val result = taskService.updateTaskForCurrentUser(updatedTask)

        assertEquals(updatedTask, result)
        verify(exactly = 1) {
            mongoOperations.upsert(any<Query>(), any<Update>(), TaskStatistics::class.java)
        }
        confirmVerified(mongoOperations)
    }

    @Test
    fun `should decrement statistics when task is marked as incomplete`() {
        val taskId = "taskId2"
        val finishedAt = LocalDateTime.now()
        val existingTask = Task(
            id = taskId,
            ownerUsername = "testUser",
            category = TaskCategory.BLUE,
            size = TaskSize.SMALL,
            title = "Test Task Incomplete",
            description = "Test Description Incomplete",
            listName = "ToDo",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            finishedAt = finishedAt
        )
        val updatedTask = existingTask.copy(finishedAt = null)

        every { taskRepository.findById(taskId) } returns Optional.of(existingTask)
        every { taskRepository.save(updatedTask) } returns updatedTask

        every {
            mongoOperations.updateFirst(any<Query>(), any<Update>(), TaskStatistics::class.java)
        } returns mockk()
        every {
            mongoOperations.remove(any<Query>(), TaskStatistics::class.java)
        } returns mockk()

        val result = taskService.updateTaskForCurrentUser(updatedTask)

        assertEquals(updatedTask, result)
        verify(exactly = 1) {
            mongoOperations.updateFirst(any<Query>(), any<Update>(), TaskStatistics::class.java)
        }
        verify(exactly = 1) {
            mongoOperations.remove(any<Query>(), TaskStatistics::class.java)
        }
        confirmVerified(mongoOperations)
    }

    @Test
    fun `should update statistics when qualifiers change on a completed task`() {
        val taskId = "taskId3"
        val finishedAt = LocalDateTime.now()
        val existingTask = Task(
            id = taskId,
            ownerUsername = "testUser",
            category = TaskCategory.BLUE,
            size = TaskSize.SMALL,
            title = "Test Task Qualifiers Change",
            description = "Test Description Qualifiers Change",
            listName = "ToDo",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            finishedAt = finishedAt
        )
        val updatedTask = existingTask.copy(category = TaskCategory.RED, size = TaskSize.BIG)

        every { taskRepository.findById(taskId) } returns Optional.of(existingTask)
        every { taskRepository.save(updatedTask) } returns updatedTask

        every {
            mongoOperations.updateFirst(any<Query>(), any<Update>(), TaskStatistics::class.java)
        } returns mockk()
        every {
            mongoOperations.upsert(any<Query>(), any<Update>(), TaskStatistics::class.java)
        } returns mockk()
        every {
            mongoOperations.remove(any<Query>(), TaskStatistics::class.java)
        } returns mockk()

        val result = taskService.updateTaskForCurrentUser(updatedTask)

        assertEquals(updatedTask, result)
        verify(exactly = 1) {
            mongoOperations.updateFirst(any<Query>(), any<Update>(), TaskStatistics::class.java)
        }
        verify(exactly = 1) {
            mongoOperations.upsert(any<Query>(), any<Update>(), TaskStatistics::class.java)
        }
        verify(exactly = 1) {
            mongoOperations.remove(any<Query>(), TaskStatistics::class.java)
        }
        confirmVerified(mongoOperations)
    }

    @Test
    fun `should handle case where task is moved from finished and qualifiers change`() {
        val taskId = "taskId4"
        val finishedAt = LocalDateTime.now()
        val existingTask = Task(
            id = taskId,
            ownerUsername = "testUser",
            category = TaskCategory.BLUE,
            size = TaskSize.SMALL,
            title = "Test Task Edge Case",
            description = "Test Description Edge Case",
            listName = "ToDo",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            finishedAt = finishedAt
        )
        val updatedTask = existingTask.copy(
            finishedAt = null,
            category = TaskCategory.RED,
            size = TaskSize.BIG
        )

        every { taskRepository.findById(taskId) } returns Optional.of(existingTask)
        every { taskRepository.save(updatedTask) } returns updatedTask

        every {
            mongoOperations.updateFirst(any<Query>(), any<Update>(), TaskStatistics::class.java)
        } returns mockk()
        every {
            mongoOperations.remove(any<Query>(), TaskStatistics::class.java)
        } returns mockk()

        val result = taskService.updateTaskForCurrentUser(updatedTask)

        assertEquals(updatedTask, result)
        verify(exactly = 1) {
            mongoOperations.updateFirst(any<Query>(), any<Update>(), TaskStatistics::class.java)
        }
        verify(exactly = 1) {
            mongoOperations.remove(any<Query>(), TaskStatistics::class.java)
        }
        verify(exactly = 0) {
            mongoOperations.upsert(any<Query>(), any<Update>(), TaskStatistics::class.java)
        }
        confirmVerified(mongoOperations)
    }
}
