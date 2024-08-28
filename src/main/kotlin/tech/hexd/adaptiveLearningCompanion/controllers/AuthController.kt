package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.hexd.adaptiveLearningCompanion.repositories.AppUser
import tech.hexd.adaptiveLearningCompanion.repositories.AppUserRepository
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil

@RestController
@RequestMapping("/auth")
class AuthController @Autowired constructor(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<*> {
        val appUser = AppUser(
            username = registerRequest.username,
            password = passwordEncoder.encode(registerRequest.password),
            roles = listOf("ROLE_USER")
        )
        appUserRepository.save(appUser)
        return ResponseEntity.ok("User registered successfully")
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<*> {
        val user = appUserRepository.findByUsername(loginRequest.username)
            ?: return ResponseEntity.badRequest().body("User not found")

        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            return ResponseEntity.badRequest().body("Invalid password")
        }

        val token = jwtUtil.generateToken(user.username)
        return ResponseEntity.ok(mapOf("token" to token))
    }
}

data class RegisterRequest(val username: String, val password: String)
data class LoginRequest(val username: String, val password: String)
