package controllers

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import tech.hexd.adaptiveLearningCompanion.AdaptiveLearningCompanionApplication
import tech.hexd.adaptiveLearningCompanion.repositories.Task

@SpringBootTest(classes = [AdaptiveLearningCompanionApplication::class])
class TaskControllerTest: BaseControllerTest() {

    private val testTaskCreateRequest = createTestTaskCreateRequest()
    private val testSavedTaskResponse = createTestSavedTaskResponse()

    @BeforeEach
    fun setup() {
        super.baseSetup()
    }

    @AfterEach
    fun cleanup() {
        super.baseCleanup()
    }

    @Test
    @WithMockUser(username = "someUsername", roles = ["USER"])
    fun `should create a task when calling createNewTask`() {
//        whenever(taskRepository.save(any<Task>())).thenReturn(testSavedTaskResponse)

//        this.performAuthenticatedPost("/api/tasks/new", testTaskCreateRequest)
//            .andExpect(MockMvcResultMatchers.status().isCreated)
//            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    @WithMockUser(username = "someUsername", roles = ["USER"])
    fun `should retrieve all tasks for user`() {
        val tasks = List(3) { generateRandomTaskFor(testUsername) }
//        whenever(taskRepository.findByOwnerUsername(any<String>())).thenReturn(tasks)

//        this.performAuthenticatedGet("/api/tasks/")
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize<Any>(3)))
//            .andExpect(MockMvcResultMatchers.jsonPath("$").value(matchesTaskList(tasks))
//            )
    }

    @Test
    @WithMockUser(username = "someUsername", roles = ["USER"])
    fun `should return empty list when retrieving all tasks but no tasks available`() {
        val tasks = emptyList<Task>()
//        whenever(taskRepository.findByOwnerUsername(any<String>())).thenReturn(tasks)

//        this.performAuthenticatedGet("/api/tasks/")
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(MockMvcResultMatchers.content().json(jacksonObjectMapper().writeValueAsString(tasks))
//            )
    }
}
