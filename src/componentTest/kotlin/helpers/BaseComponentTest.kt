package helpers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.crypto.password.PasswordEncoder
import tech.hexd.adaptiveLearningCompanion.AdaptiveLearningCompanionApplication
import tech.hexd.adaptiveLearningCompanion.controllers.UserController
import tech.hexd.adaptiveLearningCompanion.controllers.dto.LoginRequest
import tech.hexd.adaptiveLearningCompanion.repositories.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.exp
import kotlin.math.min

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@AutoConfigureDataMongo
@SpringBootTest(
    classes = [AdaptiveLearningCompanionApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
abstract class BaseComponentTest {

    @LocalServerPort
    private val port: Int = 0

    @BeforeEach
    fun baseSetup() {
        RestAssured.port = port
        RestAssured.baseURI = "http://localhost"
    }

    @AfterEach
    fun baseCleanup() {
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

    @Autowired
    protected lateinit var passwordEncoder: PasswordEncoder

    companion object {
        const val TEST_TOKEN = "someValidToken"
        const val TEST_USERNAME = "someUsername"
        const val TEST_PASSWORD = "somePassword"
        const val TEST_USER_ID = "someUserId"

        protected val logger: Logger = LoggerFactory.getLogger(UserController::class.java)
    }

    protected val testToken = TEST_TOKEN
    protected val testUsername = TEST_USERNAME
    protected val testPassword = TEST_PASSWORD
    protected val testUserId = TEST_USER_ID

    private val objectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false)
        .registerModule(SimpleModule().apply {
            addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            addSerializer(LocalTime::class.java, LocalTimeSerializer(DateTimeFormatter.ISO_LOCAL_TIME))
            addDeserializer(LocalTime::class.java, LocalTimeDeserializer(DateTimeFormatter.ISO_LOCAL_TIME))
        })


    protected fun registerUser(username: String, password: String, isAdmin: Boolean = false) {
        val roles = if (isAdmin) {
            listOf("ROLE_ADMIN")
        } else {
            listOf("ROLE_USER")
        }
        appUserRepository.save(AppUser(null, username, passwordEncoder.encode(password), roles))
    }

    protected fun getUserJwt(username: String, password: String): String {
        val response = Given {
            contentType(ContentType.JSON)
            body(LoginRequest(username = username, password = password))
        } When {
            post("/auth/login")
        } Then {
            contentType(ContentType.JSON)
            statusCode(200).onFailMessage("Error in getUserJwt for ${username}:${password}")
        } Extract {
            response()
        }
        return response.jsonPath().getString("token")
    }

    protected fun generateRandomTaskFor(username: String) = Task(
        id = UUID.randomUUID().toString(),
        ownerUsername = username,
        category = TaskCategory.entries.toTypedArray().random(),
        size = TaskSize.entries.toTypedArray().random(),
        title = generateRandomTitle(),
        description = generateRandomDescription(),
        startTime = generateRandomTime(),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        reusable = false,
    )

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

    private fun generateRandomDateTime(): LocalDateTime {
        val year = 2024
        val month = Random().nextInt(1, 13)
        val day = Random().nextInt(1, 28)
        val hour = Random().nextInt(0, 24)
        val minute = 0
        val second = 0
        return LocalDateTime.of(year, month, day, hour, minute, second)
    }

    private fun generateRandomTime(): LocalTime {
        val hour = Random().nextInt(0, 24)
        val minute = listOf(0, 15, 30, 45).random()
        return LocalTime.of(hour, minute, 0)
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

    fun matchesTask(expected: Task): TypeSafeMatcher<Map<String?, Any>> {
        return object : TypeSafeMatcher<Map<String?, Any>>() {

            override fun matchesSafely(actualJson: Map<String?, Any>): Boolean {
                val actual = objectMapper.convertValue(actualJson, Task::class.java)
                return tasksEqual(expected, actual)
            }

            override fun describeTo(description: Description) {
                description.appendText("A task matching: ")
                    .appendText("id=").appendValue(expected.id)
            }
        }
    }

    fun String.matchesShorterSubstring(expected: String): Boolean {
        val minLen = min(this.length, expected.length)
        return this.substring(0, minLen) == expected.substring(0, minLen)
    }

    private fun tasksEqual(actual: Task, expected: Task): Boolean {
        return actual.id == expected.id &&
                actual.ownerUsername == expected.ownerUsername &&
                actual.category == expected.category &&
                actual.size == expected.size &&
                actual.title == expected.title &&
                actual.description == expected.description &&
                actual.reusable == expected.reusable
//                areLocalDateTimesEqual(actual.createdAt, expected.createdAt) &&
//                areLocalDateTimesEqual(actual.updatedAt, expected.updatedAt) &&
//                areLocalTimesEqual(actual.startTime, expected.startTime)
    }

    private fun areLocalDateTimesEqual(dt1: LocalDateTime, dt2: LocalDateTime): Boolean {
        return dt1.truncatedTo(ChronoUnit.MILLIS) == dt2.truncatedTo(ChronoUnit.MILLIS)
    }

    private fun areLocalTimesEqual(dt1: LocalTime?, dt2: LocalTime?): Boolean {
        if (dt1 == null || dt2 == null) {
            return dt1 == dt2
        }
        return dt1.truncatedTo(ChronoUnit.MINUTES) == dt2.truncatedTo(ChronoUnit.MINUTES)
    }
}
