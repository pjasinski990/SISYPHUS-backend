package controllers

import helpers.BaseComponentTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import space.hexd.sisyphusBackend.controllers.dto.TaskCreateRequest
import space.hexd.sisyphusBackend.controllers.dto.TaskUpdateRequest

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
        val createRequest = TaskCreateRequest.fromTask(randomTask)

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $testUserJwt")
            body(createRequest)
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
            body("[0]", matchesTask(savedTask))
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
    fun `should return empty list when no tasks available`() {
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

    @Test
    fun `should update task for user`() {
        val savedTask = taskRepository.save(generateRandomTaskFor(testUsername))
        val updateRequest = TaskUpdateRequest.fromTask(savedTask).copy(
            title = "Updated Title",
            description = "Updated Description"
        )
        val updatedTask = updateRequest.applyTo(savedTask)

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $testUserJwt")
            body(updateRequest)
        } When {
            put("/api/tasks/")
        } Then {
            contentType(ContentType.JSON)
            statusCode(HttpStatus.OK.value())
            body("task", matchesTask(updatedTask))
        }
    }
}
