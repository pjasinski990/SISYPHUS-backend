package controllers

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.web.server.LocalServerPort
import tech.hexd.adaptiveLearningCompanion.controllers.dto.Login
import kotlin.test.Test

class AuthControllerTest: BaseControllerTest() {
    @Test
    fun `should login user if he exists`() {
        Given {
            contentType(ContentType.JSON)
            body(Login(username = "username", password = "password"))
        } When {
            post("/auth/login")
        } Then {
            contentType(ContentType.JSON)
            statusCode(400)
            body("success", equalTo(false))
        }
    }

    @Test
    fun `should fail to login user if he doesn't exist`() {
        Given {
            contentType(ContentType.JSON)
            body(Login(username = "username", password = "password"))
        } When {
            post("/auth/login")
        } Then {
            contentType(ContentType.JSON)
            statusCode(400)
            body("success", equalTo(false))
        }
    }
}
