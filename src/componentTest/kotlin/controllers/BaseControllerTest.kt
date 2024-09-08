package controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.restassured.RestAssured
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import tech.hexd.adaptiveLearningCompanion.AdaptiveLearningCompanionApplication
import tech.hexd.adaptiveLearningCompanion.controllers.UserController
import tech.hexd.adaptiveLearningCompanion.controllers.dto.TaskCreateRequest
import tech.hexd.adaptiveLearningCompanion.repositories.*
import tech.hexd.adaptiveLearningCompanion.services.UserDetailsServiceImpl
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(
    classes = [AdaptiveLearningCompanionApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
abstract class BaseControllerTest {

    @LocalServerPort
    private val port: Int = 0

    @BeforeEach
    fun setup() {
        RestAssured.port = port
        RestAssured.baseURI = "http://localhost"
    }

    @AfterEach
    fun cleanup() {
        appUserRepository.deleteAll()
        taskRepository.deleteAll()
        dailyPlanRepository.deleteAll()
    }

    @Autowired
    protected lateinit var appUserRepository: AppUserRepository

    @Autowired
    protected lateinit var taskRepository: TaskRepository

    @Autowired
    protected lateinit var dailyPlanRepository: DailyPlanRepository

    companion object {
        const val TEST_TOKEN = "someValidToken"
        const val TEST_USERNAME = "someUsername"
        const val TEST_PASSWORD = "somePassword"
        const val TEST_USER_ID = "someUserId"

        protected val logger: Logger = LoggerFactory.getLogger(UserController::class.java)
    }

    protected val objectMapper = jacksonObjectMapper()

    protected val testToken = TEST_TOKEN
    protected val testUsername = TEST_USERNAME
    protected val testPassword = TEST_PASSWORD
    protected val testUserId = TEST_USER_ID

    @BeforeEach
    fun baseSetup() {
//        every { jwtUtil.validateToken(testToken) } returns true
//        every { jwtUtil.extractUsername(testToken) } returns testUsername
//        every { jwtUtil.extractRoles(testToken) } returns arrayListOf("ROLE_USER")
    }

//    protected fun performAuthenticatedPost(url: String, content: Any): ResultActions =
//        mockMvc.perform(
//            MockMvcRequestBuilders.post(url)
//                .header("Authorization", "Bearer $testToken")
//                .with(csrf())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(jacksonObjectMapper().writeValueAsString(content))
//        )
//
//    protected fun performAuthenticatedGet(url: String): ResultActions =
//        mockMvc.perform(
//            MockMvcRequestBuilders.get(url)
//                .header("Authorization", "Bearer $testToken")
//                .with(csrf())
//        )
//
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
    ) = Task(
        id = id,
        ownerUsername = testUsername,
        category = category,
        size = size,
        title = title,
        description = description,
        reusable = false,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    protected fun generateRandomTaskFor(username: String): Task {
        return Task(
            id = UUID.randomUUID().toString(),
            ownerUsername = username,
            category = TaskCategory.entries.toTypedArray().random(),
            size = TaskSize.entries.toTypedArray().random(),
            title = generateRandomTitle(),
            description = generateRandomDescription(),
            reusable = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
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

    fun matchesTaskList(expected: List<Task>) = object : BaseMatcher<List<Map<String, Any>>>() {
        override fun matches(actual: Any?): Boolean {
            if (actual !is List<*>) return false
            if (actual.size != expected.size) return false

            return actual.zip(expected).all { (actualItem, expectedTask) ->
                actualItem is Map<*, *> && actualItem["id"] == expectedTask.id
            }
        }

        override fun describeTo(description: Description) {
            description.appendText("a list of ${expected.size} tasks with ids: ")
                .appendValueList("[", ", ", "]", expected.map { it.id })
        }

        override fun describeMismatch(item: Any?, description: Description) {
            if (item !is List<*>) {
                description.appendText("was not a list")
                return
            }
            description.appendText("was a list of ${item.size} tasks with ids: ")
                .appendValueList("[", ", ", "]", item.map { (it as? Map<*, *>)?.get("id") })
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
