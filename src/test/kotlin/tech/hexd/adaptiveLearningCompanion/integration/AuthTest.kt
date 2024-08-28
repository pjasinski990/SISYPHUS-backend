import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tech.hexd.adaptiveLearningCompanion.AdaptiveLearningCompanionApplication
import tech.hexd.adaptiveLearningCompanion.repositories.AppUser
import tech.hexd.adaptiveLearningCompanion.repositories.AppUserRepository
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil

@SpringBootTest(classes = [AdaptiveLearningCompanionApplication::class])
@AutoConfigureMockMvc
class AuthControllerTest {
    @BeforeEach
    fun setup(): Unit {
        whenever(appUserRepository.findByUsername(any())).thenReturn(mockAppUser)
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @MockBean
    private lateinit var appUserRepository: AppUserRepository;

    @MockBean
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun `should allow authenticated endpoint with valid jwt`() {
        val token = jwtUtil.generateToken("testuser")

        mockMvc.perform(get("/user/profile")
            .header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `should not allow authenticated endpoint without jwt`() {
        mockMvc.perform(get("/user/profile"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should allow admin endpoint access for admin role`() {
        val adminToken = jwtUtil.generateAdminToken("someAdminUser")
        whenever(appUserRepository.findByUsername(any())).thenReturn(mockAppAdminUser)

        mockMvc.perform(get("/admin/dashboard")
            .header("Authorization", "Bearer $adminToken"))
            .andExpect(status().isOk)
    }

    @Test
    fun `should not allow admin endpoint for non-admin role`() {
        val userToken = jwtUtil.generateToken("user")

        mockMvc.perform(get("/admin/dashboard")
            .header("Authorization", "Bearer $userToken"))
            .andExpect(status().isForbidden)
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun setupAll() {
            mockAppUser = AppUser(
                id = "someId",
                username = "testUser",
                password = "passwordHash",
                roles = listOf("ROLE_USER")
            )
            mockAppAdminUser = AppUser(
                id = "someId",
                username = "testAdminUser",
                password = "passwordHash",
                roles = listOf("ROLE_ADMIN")
            )
        }

        private lateinit var mockAppUser: AppUser
        private lateinit var mockAppAdminUser: AppUser
    }
}