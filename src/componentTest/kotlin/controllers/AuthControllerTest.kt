package controllers

import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.Response
import org.hamcrest.CoreMatchers.equalTo
import tech.hexd.adaptiveLearningCompanion.controllers.dto.Login
import tech.hexd.adaptiveLearningCompanion.controllers.dto.Register
import tech.hexd.adaptiveLearningCompanion.repositories.AppUser
import kotlin.test.Test

class AuthControllerTest: BaseControllerTest() {
    @Test
    fun `should login user with correct password if he exists`() {
        appUserRepository.save(AppUser(null, testUsername, passwordEncoder.encode(testPassword), listOf("ROLE_USER")))
        loginAndOk(testUsername, testPassword)
    }

    @Test
    fun `should fail to login user with incorrect password`() {
        appUserRepository.save(AppUser(null, testUsername, passwordEncoder.encode(testPassword), listOf("ROLE_USER")))
        loginAndFail(testUsername, "invalid_password")
    }

    @Test
    fun `should fail to login user if he doesn't exist`() {
        loginAndFail(testUsername, testPassword)
    }

    @Test
    fun `should fail to register user if he exists`() {
        appUserRepository.save(AppUser(null, testUsername, passwordEncoder.encode(testPassword), listOf("ROLE_USER")))
        registerAndFail(testUsername, testPassword)
    }

    @Test
    fun `should register user and then login`() {
        registerAndOk(testUsername, testPassword)
        loginAndOk(testUsername, testPassword)
    }

    @Test
    fun `should fail to register twice`() {
        registerAndOk(testUsername, testPassword)
        registerAndFail(testUsername, testPassword)
    }

    private fun loginAndOk(username: String, password: String): Response {
        return Given {
            contentType(ContentType.JSON)
            body(Login(username = username, password = password))
        } When {
            post("/auth/login")
        } Then {
            contentType(ContentType.JSON)
            statusCode(200)
            body("success", equalTo(true))
        } Extract {
            response()
        }
    }

    private fun loginAndFail(username: String, password: String): Response {
        return Given {
            contentType(ContentType.JSON)
            body(Login(username = testUsername, password = "invalid"))
        } When {
            post("/auth/login")
        } Then {
            contentType(ContentType.JSON)
            statusCode(400)
            body("success", equalTo(false))
        } Extract {
            response()
        }
    }

    private fun registerAndOk(username: String, password: String): Response {
        return Given {
            contentType(ContentType.JSON)
            body(Register(username = username, password = password))
        } When {
            post("/auth/register")
        } Then {
            contentType(ContentType.JSON)
            statusCode(200)
            body("success", equalTo(true))
        } Extract {
            response()
        }
    }

    private fun registerAndFail(username: String, password: String): Response {
        return Given {
            contentType(ContentType.JSON)
            body(Register(username = testUsername, password = "invalid"))
        } When {
            post("/auth/register")
        } Then {
            contentType(ContentType.JSON)
            statusCode(400)
            body("success", equalTo(false))
        } Extract {
            response()
        }
    }
}
