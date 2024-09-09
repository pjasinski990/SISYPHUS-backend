import controllers.BaseControllerTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate


class DailyPlanControllerTest: BaseControllerTest() {
    @BeforeEach
    fun setup() {
        registerUser(testUsername, testPassword)
    }

    @Test
    fun `should return plan if exists for a date`() {
        val testDate = LocalDate.of(2024, 8, 30)
        val testDailyPlan = createRandomDailyPlanFor(TEST_USERNAME, testDate)
        dailyPlanRepository.save(testDailyPlan)

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer ${getUserJwt(testUsername, testPassword)}")
        } When {
            get("/daily-plan/${testDate}")
        } Then {
            contentType(ContentType.JSON)
            statusCode(200)
            body("plan.id", equalTo(testDailyPlan.id))
            body("plan.ownerUsername", equalTo(testDailyPlan.ownerUsername))
            body("plan.day", equalTo(testDate.toString()))
            body("plan.todo", matchesTaskList(testDailyPlan.todo))
            body("plan.done", matchesTaskList(testDailyPlan.done))
        }
    }

    @Test
    fun `should create a new plan if plan does not exist for a date`() {
        val testDate = LocalDate.of(2024, 8, 30)

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer ${getUserJwt(testUsername, testPassword)}")
        } When {
            get("/daily-plan/${testDate}")
        } Then {
            contentType(ContentType.JSON)
            statusCode(200)
            body("plan.ownerUsername", equalTo(testUsername))
            body("plan.day", equalTo(testDate.toString()))
            body("plan.todo", matchesTaskList(emptyList()))
            body("plan.done", matchesTaskList(emptyList()))
        }
    }
}
