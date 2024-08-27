package tech.hexd.adaptiveLearningCompanion.integration.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import tech.hexd.adaptiveLearningCompanion.controllers.TaskController
import tech.hexd.adaptiveLearningCompanion.controllers.TaskCreateRequest
import tech.hexd.adaptiveLearningCompanion.repositories.*
import tech.hexd.adaptiveLearningCompanion.services.UserDetailsServiceImpl
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil

@WebMvcTest(TaskController::class)
class TaskControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var taskRepository: TaskRepository

    @MockBean
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @MockBean
    private lateinit var jwtUtil: JwtUtil

    private lateinit var testUsername: String
    private lateinit var testUserId: String
    private lateinit var testTaskCreateRequest: TaskCreateRequest
    private lateinit var testSavedTaskResponse: Task
    private lateinit var testToken: String

    @BeforeEach
    fun setup() {
        testUsername = "someUsername"
        testUserId = "someUserId"
        testTaskCreateRequest = TaskCreateRequest(
            category = TaskCategory.BLUE,
            size = TaskSize.BIG,
            description = "finish implementation of TaskController tests"
        )
        testSavedTaskResponse = Task(
            id = "someSavedTaskId",
            category = TaskCategory.BLUE,
            size = TaskSize.BIG,
            description = "finish implementation of TaskController tests"
        )

        testToken = "someValidToken"
        whenever(jwtUtil.validateToken(testToken)).thenReturn(true)
        whenever(jwtUtil.extractUsername(testToken)).thenReturn(testUsername)
        whenever(jwtUtil.extractRoles(testToken)).thenReturn(arrayListOf("ROLE_USER"))
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `should create a task`() {
        whenever(taskRepository.save(any<Task>())).thenReturn(testSavedTaskResponse)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/tasks/new")
                .header("Authorization", "Bearer $testToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(testTaskCreateRequest)))
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
    }
}