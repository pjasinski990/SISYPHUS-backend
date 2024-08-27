package tech.hexd.adaptiveLearningCompanion.integration.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import tech.hexd.adaptiveLearningCompanion.controllers.TaskController
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskRepository

@WebMvcTest(TaskController::class)
class TaskControllerTest: BaseControllerTest() {
    @MockBean
    private lateinit var taskRepository: TaskRepository

    private val testTaskCreateRequest = createTestTaskCreateRequest()
    private val testSavedTaskResponse = createTestSavedTaskResponse()

    @BeforeEach
    fun setup() {
        this.baseSetup()
    }

    @Test
    @WithMockUser(username = "someUsername", roles = ["USER"])
    fun `should create a task`() {
        whenever(taskRepository.save(any<Task>())).thenReturn(testSavedTaskResponse)

        this.performPost("/api/tasks/new", testTaskCreateRequest)
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    @WithMockUser(username = "someUsername", roles = ["USER"])
    fun `should retrieve all tasks`() {
        val tasks = List(3) { generateRandomTask() }
        whenever(taskRepository.findAll()).thenReturn(tasks)

        this.performGet("/api/tasks/")
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(jacksonObjectMapper().writeValueAsString(tasks))
            )
    }
}
