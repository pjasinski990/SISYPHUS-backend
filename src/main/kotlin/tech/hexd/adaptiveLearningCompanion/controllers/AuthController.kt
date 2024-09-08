package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.hexd.adaptiveLearningCompanion.controllers.dto.Login
import tech.hexd.adaptiveLearningCompanion.controllers.dto.Register
import tech.hexd.adaptiveLearningCompanion.repositories.AppUser
import tech.hexd.adaptiveLearningCompanion.repositories.AppUserRepository
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil
import tech.hexd.adaptiveLearningCompanion.util.ResponseForger

@RestController
@RequestMapping("/auth")
class AuthController @Autowired constructor(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/register")
    fun register(@RequestBody registerRequest: Register): ResponseEntity<*> {
        if (appUserRepository.findByUsername(registerRequest.username) != null) {
            return ResponseForger().badRequestFailure("User already exists").build()
        }

        this.registerUser(registerRequest.username, registerRequest.password)
        return ResponseForger().ok("User registered successfully").build()
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: Login): ResponseEntity<*> {
        val user = appUserRepository.findByUsername(loginRequest.username)
            ?: return ResponseForger().badRequestFailure("User does not exist").build()

        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            return ResponseForger().badRequestFailure("Invalid password").build()
        }

        val token = jwtUtil.generateToken(user.username)
        return ResponseForger().ok("Login successful").withField("token", token).build()
    }

    private fun registerUser(username: String, password: String) {
        val appUser = AppUser(
            username = username,
            password = passwordEncoder.encode(password),
            roles = listOf("ROLE_USER")
        )
        appUserRepository.save(appUser)
    }
}
