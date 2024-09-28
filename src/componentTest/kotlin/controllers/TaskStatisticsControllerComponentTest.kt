package controllers

import helpers.BaseComponentTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import tech.hexd.adaptiveLearningCompanion.controllers.dto.TaskUpdateRequest
import java.time.LocalDate

class TaskStatisticsControllerComponentTest : BaseComponentTest() {
    private lateinit var testUserJwt: String

    @BeforeEach
    fun setup() {
        registerUser(testUsername, testPassword)
        testUserJwt = getUserJwt(testUsername, testPassword)
    }

    @Test
    fun `should retrieve task statistics for user`() {
        val date = LocalDate.of(2024, 9, 25)
        val task = generateRandomTaskFor(testUsername).copy(finishedAt = null)
        logger.warn(task.toString())
        val savedTask = taskRepository.save(task)

        val updateRequest = TaskUpdateRequest.fromTask(savedTask).copy(finishedAt = date.atStartOfDay())
        logger.warn(updateRequest.toString())

        val response = Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $testUserJwt")
            body(updateRequest)
        } When {
            put("/api/tasks/")
        } Then {
            statusCode(HttpStatus.OK.value())
            body("task", matchesTask(task))
        } Extract {
            response()
        }
        logger.warn(response.toString())

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $testUserJwt")
            param("date", date.toString())
        } When {
            get("/api/task-statistics/")
        } Then {
            contentType(ContentType.JSON)
            statusCode(HttpStatus.OK.value())
            body("size()", equalTo(1))
            body("[0].ownerUsername", equalTo(testUsername))
            body("[0].date", equalTo(date.toString()))
            body("[0].category", equalTo(task.category.toString()))
            body("[0].size", equalTo(task.size.toString()))
            body("[0].count", equalTo(1))
        }
    }
}
