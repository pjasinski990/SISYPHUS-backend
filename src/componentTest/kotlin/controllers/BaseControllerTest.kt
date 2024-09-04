package controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.whenever
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import tech.hexd.adaptiveLearningCompanion.controllers.TaskCreateRequest
import tech.hexd.adaptiveLearningCompanion.controllers.UserController
import tech.hexd.adaptiveLearningCompanion.repositories.*
import tech.hexd.adaptiveLearningCompanion.services.UserDetailsServiceImpl
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil
import java.time.LocalDate
import java.util.*

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@AutoConfigureMockMvc
@AutoConfigureDataMongo
@EnableAutoConfiguration(exclude=[MongoAutoConfiguration::class])
abstract class BaseControllerTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @MockBean
    protected lateinit var jwtUtil: JwtUtil

    @MockBean
    protected lateinit var appUserRepository: AppUserRepository

    @MockBean
    protected lateinit var taskRepository: TaskRepository

    @MockBean
    protected lateinit var dailyPlanRepository: DailyPlanRepository

    @MockBean
    protected lateinit var userDetailsService: UserDetailsServiceImpl

    companion object {
        const val TEST_TOKEN = "someValidToken"
        const val TEST_USERNAME = "someUsername"
        const val TEST_USER_ID = "someUserId"

        protected val logger: Logger = LoggerFactory.getLogger(UserController::class.java)
    }

    protected val objectMapper = jacksonObjectMapper()

    protected val testToken = BaseControllerTest.TEST_TOKEN
    protected val testUsername = BaseControllerTest.TEST_USERNAME
    protected val testUserId = BaseControllerTest.TEST_USER_ID

    @BeforeEach
    fun baseSetup() {
        whenever(jwtUtil.validateToken(testToken)).thenReturn(true)
        whenever(jwtUtil.extractUsername(testToken)).thenReturn(testUsername)
        whenever(jwtUtil.extractRoles(testToken)).thenReturn(arrayListOf("ROLE_USER"))
    }

    protected fun performAuthenticatedPost(url: String, content: Any): ResultActions =
        mockMvc.perform(
            MockMvcRequestBuilders.post(url)
                .header("Authorization", "Bearer $testToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(content))
        )

    protected fun performAuthenticatedGet(url: String): ResultActions =
        mockMvc.perform(
            MockMvcRequestBuilders.get(url)
                .header("Authorization", "Bearer $testToken")
                .with(csrf())
        )

    protected fun createTestTaskCreateRequest(
        category: TaskCategory = TaskCategory.BLUE,
        size: TaskSize = TaskSize.BIG,
        title: String = "TaskController tests implementation",
        description: String = "finish implementation of TaskController tests"
    ) = TaskCreateRequest(category, size, title, description)

    protected fun createTestSavedTaskResponse(
        id: String = "someSavedTaskId",
        category: TaskCategory = TaskCategory.BLUE,
        size: TaskSize = TaskSize.BIG,
        title: String = "TaskController tests implementation",
        description: String = "finish implementation of TaskController tests"
    ) = Task(id, testUsername, category, size, title, description)

    protected fun generateRandomTaskFor(username: String): Task {
        return Task(
            id = UUID.randomUUID().toString(),
            ownerUsername = username,
            category = TaskCategory.entries.toTypedArray().random(),
            size = TaskSize.entries.toTypedArray().random(),
            title = generateRandomTitle(),
            description = generateRandomDescription(),
        )
    }

    protected fun matchesJsonOf(expected: Any) = object : BaseMatcher<Any>() {
        override fun matches(actual: Any?): Boolean {
            val expectedJson = objectMapper.writeValueAsString(expected)
            val actualJson = objectMapper.writeValueAsString(actual)
            return expectedJson == actualJson
        }

        override fun describeTo(description: Description) {
            description.appendText("matches JSON representation of ").appendValue(expected)
        }
    }

    private fun generateRandomTitle(): String {
        val prefixes = listOf("Task", "Issue", "Project", "Work Item", "Assignment")
        val actions = listOf("Review", "Update", "Create", "Analyze", "Fix")
        val subjects = listOf("Code", "Documentation", "Database", "UI", "API")

        return "${prefixes.random()}: ${actions.random()} ${subjects.random()}"
    }

    private fun generateRandomDescription(): String {
        val adjectives = listOf("Urgent", "Important", "Routine", "Critical", "Optional")
        val verbs = listOf("Implement", "Debug", "Refactor", "Optimize", "Test")
        val nouns = listOf("feature", "bug", "module", "function", "algorithm")

        return "${adjectives.random()} task: ${verbs.random()} the ${nouns.random()}"
    }

    protected fun createRandomDailyPlanFor(username: String, date: LocalDate): DailyPlan {
        val todo = List(3) { generateRandomTaskFor(username) }
        val done = List(3) { generateRandomTaskFor(username) }
        return DailyPlan(
            id = UUID.randomUUID().toString(),
            ownerUsername = username,
            day = date,
            todo = todo,
            done = done,
        )
    }
}
