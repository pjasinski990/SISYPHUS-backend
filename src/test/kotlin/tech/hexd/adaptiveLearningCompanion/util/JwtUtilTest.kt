package tech.hexd.adaptiveLearningCompanion.util

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import tech.hexd.adaptiveLearningCompanion.AdaptiveLearningCompanionApplication
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JwtUtilTest {
    private lateinit var jwtUtil: JwtUtil;

    @BeforeEach
    fun setUp() {
        jwtUtil = JwtUtil()
    }

    @Test
    fun `should validate correct token`() {
        val testUsername = "someUser"
        val token = jwtUtil.generateToken(testUsername)

        val res = jwtUtil.validateToken(token)

        assertTrue(res)
    }

    @Test
    fun `should fail to validate tampered token`() {
        val testUsername = "someUser"
        val token = jwtUtil.generateToken(testUsername)
        val tamperedToken = token + 'a'

        val res = jwtUtil.validateToken(tamperedToken)

        assertFalse(res)
    }

    @Test
    fun `should extract username from token`() {
        val testUsername = "someUser"
        val token = jwtUtil.generateToken(testUsername)

        val res = jwtUtil.extractUsername(token)

        assertEquals(testUsername, res)
    }
}
