package controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import tech.hexd.adaptiveLearningCompanion.AdaptiveLearningCompanionApplication
import tech.hexd.adaptiveLearningCompanion.repositories.DailyPlan
import java.time.LocalDate.of

@SpringBootTest(classes = [AdaptiveLearningCompanionApplication::class])
class DailyPlanControllerTest: BaseControllerTest() {

    @BeforeEach
    fun setup() {
        this.baseSetup()
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, roles = ["USER"])
    fun `should return plan if exists for a date`() {
        val testDate = of(2024, 8, 30)
        val testDailyPlan = createRandomDailyPlanFor(testUsername, testDate)
        whenever(dailyPlanRepository.findByOwnerUsernameAndDay(testUsername, testDate)).thenReturn(testDailyPlan)

        this.performAuthenticatedGet("/daily-plan/${testDate}")
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.plan.id").value(testDailyPlan.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.plan.ownerUsername").value(testDailyPlan.ownerUsername))
            .andExpect(MockMvcResultMatchers.jsonPath("$.plan.day").value(testDate.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.plan.todo").value(matchesTaskList(testDailyPlan.todo)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.plan.done").value(matchesTaskList(testDailyPlan.done)))
    }

    @Test
    @WithMockUser(username = "someUsername", roles = ["USER"])
    fun `should create a new plan if plan doesn't exist for a date`() {
        val testDate = of(2024, 8, 30)
        val expectedNewPlan = DailyPlan.newEmptyForUserAndDate(testUsername, testDate)
        whenever(dailyPlanRepository.findByOwnerUsernameAndDay(testUsername, testDate)).thenReturn(null)
        whenever(dailyPlanRepository.save(any<DailyPlan>())).thenReturn(expectedNewPlan)

        this.performAuthenticatedGet("/daily-plan/${testDate}")
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.plan.ownerUsername").value(expectedNewPlan.ownerUsername))
            .andExpect(MockMvcResultMatchers.jsonPath("$.plan.day").value(testDate.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.plan.todo").value(matchesJsonOf(expectedNewPlan.todo)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.plan.done").value(matchesJsonOf(expectedNewPlan.done)))
    }
}
