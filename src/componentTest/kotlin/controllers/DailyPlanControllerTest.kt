import controllers.BaseControllerTest
import io.mockk.every
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import tech.hexd.adaptiveLearningCompanion.AdaptiveLearningCompanionApplication
import tech.hexd.adaptiveLearningCompanion.repositories.DailyPlan
import java.time.LocalDate

@SpringBootTest(
    classes = [AdaptiveLearningCompanionApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class DailyPlanControllerTest: BaseControllerTest() {
    companion object {
        @Container
        private val mongoDBContainer = MongoDBContainer("mongo:4.4.6")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }

        const val TEST_USERNAME = "testUser"
    }

    @LocalServerPort
    private val port: Int = 0

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = ["USER"])
    fun `should return plan if exists for a date`() {
        val testDate = LocalDate.of(2024, 8, 30)
        val testDailyPlan = createRandomDailyPlanFor(TEST_USERNAME, testDate)
//        every { dailyPlanRepository.findByOwnerUsernameAndDay(TEST_USERNAME, testDate) } returns testDailyPlan

        Given {
            auth().basic(TEST_USERNAME, "testPassword")
        } When {
            get("/daily-plan/${testDate}")
        } Then {
            statusCode(200)
            body("success", equalTo(true))
            body("plan.id", equalTo(testDailyPlan.id))
            body("plan.ownerUsername", equalTo(testDailyPlan.ownerUsername))
            body("plan.day", equalTo(testDate.toString()))
            body("plan.todo", matchesTaskList(testDailyPlan.todo))
            body("plan.done", matchesTaskList(testDailyPlan.done))
        }
    }

    @Test
    @WithMockUser(username = "someUsername", roles = ["USER"])
    fun `should create a new plan if plan doesn't exist for a date`() {
        val testDate = LocalDate.of(2024, 8, 30)
        val expectedNewPlan = DailyPlan.newEmptyForUserAndDate(TEST_USERNAME, testDate)
//        every { dailyPlanRepository.findByOwnerUsernameAndDay(TEST_USERNAME, testDate) } returns null
//        every { dailyPlanRepository.save(any<DailyPlan>()) } returns expectedNewPlan

        When {
            get("/daily-plan/${testDate}")
        } Then {
            statusCode(200)
            body("success", equalTo(true))
            body("plan.ownerUsername", equalTo(expectedNewPlan.ownerUsername))
            body("plan.day", equalTo(testDate.toString()))
            body("plan.todo", matchesJsonOf(expectedNewPlan.todo))
            body("plan.done", matchesJsonOf(expectedNewPlan.done))
        }
    }
}