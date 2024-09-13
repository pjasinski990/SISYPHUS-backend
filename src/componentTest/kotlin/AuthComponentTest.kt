import helpers.BaseComponentTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class AuthComponentTest: BaseComponentTest() {
    @Test
    fun `should allow authenticated endpoint with valid jwt`() {
        registerUser(testUsername, testPassword)
        val jwt = getUserJwt(testUsername, testPassword)

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $jwt")
        } When {
            get("/user/profile")
        } Then {
            contentType(ContentType.JSON)
            statusCode(HttpStatus.OK.value())
        }
    }

    @Test
    fun `should not allow authenticated endpoint without jwt`() {
        Given {
            contentType(ContentType.JSON)
        } When {
            get("/user/profile")
        } Then {
            statusCode(HttpStatus.UNAUTHORIZED.value())
        }
    }

    @Test
    fun `should allow admin endpoint access for admin role`() {
        registerUser(testUsername, testPassword, isAdmin = true)
        val jwt = getUserJwt(testUsername, testPassword)

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $jwt")
        } When {
            get("/admin/dashboard")
        } Then {
            statusCode(HttpStatus.OK.value())
        }
    }

    @Test
    fun `should not allow admin endpoint for non-admin role`() {
        registerUser(testUsername, testPassword, isAdmin = false)
        val jwt = getUserJwt(testUsername, testPassword)

        Given {
            contentType(ContentType.JSON)
            header("Authorization", "Bearer $jwt")
        } When {
            get("/admin/dashboard")
        } Then {
            statusCode(HttpStatus.UNAUTHORIZED.value())
        }
    }
}
