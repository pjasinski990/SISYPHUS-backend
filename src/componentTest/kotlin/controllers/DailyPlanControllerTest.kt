package controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import tech.hexd.adaptiveLearningCompanion.AdaptiveLearningCompanionApplication
import java.time.LocalDate.of

@AutoConfigureMockMvc
@SpringBootTest(classes = [AdaptiveLearningCompanionApplication::class])
class DailyPlanControllerTest: BaseControllerTest() {

    @BeforeEach
    fun setup() {
        this.baseSetup()
    }

    @Test
    @WithMockUser(username = "someUsername", roles = ["USER"])
    fun `should return plan if exists for a date`() {
        val testDate = of(2024, 8, 30)
        val testDailyPlan = createRandomDailyPlanFor(testUsername, testDate)
        whenever(dailyPlanRepository.findByOwnerUsernameAndDay(testUsername, testDate)).thenReturn(testDailyPlan)

        this.performAuthenticatedGet("/daily-plan/${testDate}")
            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.content().json(jacksonObjectMapper().writeValueAsString(testDailyPlan)))
    }
}
