package controllers

import BaseComponentTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import tech.hexd.adaptiveLearningCompanion.controllers.dto.TaskCreateRequest

class TaskControllerComponentTest: BaseComponentTest() {
    private lateinit var testUserJwt: String

    @BeforeEach
    fun setup() {
        registerUser(testUsername, testPassword)
        testUserJwt = getUserJwt(testUsername, testPassword)
    }

    @Test
    fun `should create a task when calling createNewTask`() {
        val randomTask = generateRandomTaskFor(testUsername)
        val body = TaskCreateRequest.fromTask(randomTask)

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $testUserJwt")
            body(body)
        } When {
            post("/api/tasks/")
        } Then {
            contentType(ContentType.JSON)
            statusCode(HttpStatus.CREATED.value())
            body("title", equalTo(randomTask.title))
            body("description", equalTo(randomTask.description))
            body("category", equalTo(randomTask.category.toString()))
            body("size", equalTo(randomTask.size.toString()))
        }
    }

    @Test
    fun `should retrieve correct task for user`() {
        val task = generateRandomTaskFor(testUsername)
        val savedTask = taskRepository.save(task)

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $testUserJwt")
        } When {
            get("/api/tasks/")
        } Then {
            contentType(ContentType.JSON)
            statusCode(HttpStatus.OK.value())
            body("$.size()", equalTo(1))
            body("[0].id", equalTo(savedTask.id))
            body("[0].ownerUsername", equalTo(savedTask.ownerUsername))
            body("[0].category", equalTo(savedTask.category.toString()))
            body("[0].size", equalTo(savedTask.size.toString()))
            body("[0].title", equalTo(savedTask.title))
            body("[0].description", equalTo(savedTask.description))
            body("[0].reusable", equalTo(savedTask.reusable))
            body("[0].createdAt", equalsTimestampUpLowerPrecision(savedTask.createdAt.toString()))
            body("[0].updatedAt", equalsTimestampUpLowerPrecision(savedTask.updatedAt.toString()))
        }
    }

    @Test
    fun `should retrieve all tasks for user`() {
        val tasks = List(3) { generateRandomTaskFor(testUsername) }
        for (task in tasks) {
            taskRepository.save(task)
        }

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $testUserJwt")
        } When {
            get("/api/tasks/")
        } Then {
            contentType(ContentType.JSON)
            statusCode(HttpStatus.OK.value())
            body("$.size()", equalTo(3))
        }
    }

    @Test
    fun `should return empty list when retrieving all tasks but no tasks available`() {
        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $testUserJwt")
        } When {
            get("/api/tasks/")
        } Then {
            contentType(ContentType.JSON)
            statusCode(HttpStatus.OK.value())
            body("$.size()", equalTo(0))
        }
    }
}
