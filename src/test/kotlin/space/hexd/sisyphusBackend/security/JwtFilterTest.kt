package space.hexd.sisyphusBackend.security

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.BeforeEach
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import space.hexd.sisyphusBackend.services.UserDetailsServiceImpl
import space.hexd.sisyphusBackend.util.JwtUtil
import kotlin.test.Test

class JwtFilterTest {

    @MockK
    private lateinit var jwtUtil: JwtUtil

    @MockK
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @MockK
    private lateinit var filterChain: FilterChain

    private lateinit var jwtFilter: JwtFilter

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        jwtFilter = JwtFilter(jwtUtil, userDetailsService)
        every { filterChain.doFilter(any(), any()) } just Runs

        SecurityContextHolder.clearContext()
    }

    @Test
    fun `doFilter should set authentication when valid token is provided`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val jwt = "valid.jwt.token"
        val username = "someUser"
        val userDetails = User(username, "password", emptyList())

        request.addHeader("Authorization", "Bearer $jwt")

        every { jwtUtil.extractUsername(jwt) } returns username
        every { jwtUtil.validateToken(jwt) } returns true
        every { userDetailsService.loadUserByUsername(username) } returns userDetails

        jwtFilter.doFilter(request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        assert(SecurityContextHolder.getContext().authentication != null)
        assert(SecurityContextHolder.getContext().authentication.principal == userDetails)
    }

    @Test
    fun `doFilter should not set authentication when invalid token is provided`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val jwt = "invalid.jwt.token"
        val username = "someUser"

        request.addHeader("Authorization", "Bearer $jwt")

        every { jwtUtil.extractUsername(jwt) }.returns(username)
        every { jwtUtil.validateToken(jwt) }.returns(false)

        jwtFilter.doFilter(request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        assert(SecurityContextHolder.getContext().authentication == null)
    }

    @Test
    fun `doFilter should not process when Authorization header is not present`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        jwtFilter.doFilter(request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        verify {
            jwtUtil wasNot Called
            userDetailsService wasNot Called
        }
        assert(SecurityContextHolder.getContext().authentication == null)
    }

    @Test
    fun `doFilter should not process when Authorization header does not start with Bearer`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        request.addHeader("Authorization", "Basic some.jwt.token")

        jwtFilter.doFilter(request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        verify {
            jwtUtil wasNot Called
            userDetailsService wasNot Called
        }
        assert(SecurityContextHolder.getContext().authentication == null)
    }
}