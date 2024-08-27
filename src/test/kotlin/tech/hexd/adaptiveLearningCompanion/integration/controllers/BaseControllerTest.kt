package tech.hexd.adaptiveLearningCompanion.integration.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.http.server.reactive.MockServerHttpRequest.post
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import tech.hexd.adaptiveLearningCompanion.controllers.TaskCreateRequest
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskCategory
import tech.hexd.adaptiveLearningCompanion.repositories.TaskSize
import tech.hexd.adaptiveLearningCompanion.repositories.UserRepository
import tech.hexd.adaptiveLearningCompanion.services.UserDetailsServiceImpl
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil
import java.util.*

abstract class BaseControllerTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @MockBean
    protected lateinit var jwtUtil: JwtUtil

    @MockBean
    protected lateinit var userRepository: UserRepository

    @MockBean
    protected lateinit var userDetailsService: UserDetailsServiceImpl

    protected val testToken = "someValidToken"
    protected val testUsername = "someUsername"
    protected val testUserId = "someUserId"

    @BeforeEach
    fun baseSetup() {
        whenever(jwtUtil.validateToken(testToken)).thenReturn(true)
        whenever(jwtUtil.extractUsername(testToken)).thenReturn(testUsername)
        whenever(jwtUtil.extractRoles(testToken)).thenReturn(arrayListOf("ROLE_USER"))
    }

    protected fun performPost(url: String, content: Any): ResultActions =
        mockMvc.perform(
            MockMvcRequestBuilders.post(url)
                .header("Authorization", "Bearer $testToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(content))
        )

    protected fun performGet(url: String): ResultActions =
        mockMvc.perform(
            MockMvcRequestBuilders.get(url)
                .header("Authorization", "Bearer $testToken")
                .with(csrf())
        )

    protected fun createTestTaskCreateRequest(
        category: TaskCategory = TaskCategory.BLUE,
        size: TaskSize = TaskSize.BIG,
        description: String = "finish implementation of TaskController tests"
    ) = TaskCreateRequest(category, size, description)

    protected fun createTestSavedTaskResponse(
        id: String = "someSavedTaskId",
        category: TaskCategory = TaskCategory.BLUE,
        size: TaskSize = TaskSize.BIG,
        description: String = "finish implementation of TaskController tests"
    ) = Task(id, category, size, description)

    protected fun generateRandomTask(): Task {
        return Task(
            id = UUID.randomUUID().toString(),
            category = TaskCategory.entries.toTypedArray().random(),
            size = TaskSize.entries.toTypedArray().random(),
            description = generateRandomDescription()
        )
    }

    private fun generateRandomDescription(): String {
        val adjectives = listOf("Urgent", "Important", "Routine", "Critical", "Optional")
        val verbs = listOf("Implement", "Debug", "Refactor", "Optimize", "Test")
        val nouns = listOf("feature", "bug", "module", "function", "algorithm")

        return "${adjectives.random()} task: ${verbs.random()} the ${nouns.random()}"
    }}
