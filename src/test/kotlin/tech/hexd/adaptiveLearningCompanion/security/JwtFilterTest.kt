package tech.hexd.adaptiveLearningCompanion.security

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import tech.hexd.adaptiveLearningCompanion.services.UserDetailsServiceImpl
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil
import kotlin.test.Test

class JwtFilterTest {

    @Mock
    private lateinit var jwtUtil: JwtUtil

    @Mock
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @Mock
    private lateinit var filterChain: FilterChain

    private lateinit var jwtFilter: JwtFilter

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        jwtFilter = JwtFilter(jwtUtil, userDetailsService)
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

        `when`(jwtUtil.extractUsername(jwt)).thenReturn(username)
        `when`(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails)
        `when`(jwtUtil.validateToken(jwt)).thenReturn(true)

        jwtFilter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
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

        `when`(jwtUtil.extractUsername(jwt)).thenReturn(username)
        `when`(jwtUtil.validateToken(jwt)).thenReturn(false)

        jwtFilter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        assert(SecurityContextHolder.getContext().authentication == null)
    }

    @Test
    fun `doFilter should not process when no Authorization header is present`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        jwtFilter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        verifyNoInteractions(jwtUtil)
        verifyNoInteractions(userDetailsService)
        assert(SecurityContextHolder.getContext().authentication == null)
    }

    @Test
    fun `doFilter should not process when Authorization header does not start with Bearer`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        request.addHeader("Authorization", "Basic some.jwt.token")

        jwtFilter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        verifyNoInteractions(jwtUtil)
        verifyNoInteractions(userDetailsService)
        assert(SecurityContextHolder.getContext().authentication == null)
    }
}