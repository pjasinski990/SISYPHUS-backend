package space.hexd.sisyphusBackend.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.hexd.sisyphusBackend.controllers.dto.*
import space.hexd.sisyphusBackend.repositories.AppUser
import space.hexd.sisyphusBackend.repositories.AppUserRepository
import space.hexd.sisyphusBackend.util.JwtUtil
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthController @Autowired constructor(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {
    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<RegisterResponse> {
        if (appUserRepository.findByUsername(registerRequest.username) != null) {
            return ResponseEntity.badRequest().body(RegisterResponse("User already exists"))
        }

        this.registerUser(registerRequest.username, registerRequest.password)
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterResponse("Registration successful"))
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        val user = appUserRepository.findByUsername(loginRequest.username)
            ?: return ResponseEntity.badRequest().body(LoginResponse("User does not exist", null, null))

        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            return ResponseEntity.badRequest().body(LoginResponse("Invalid password", null, null))
        }

        val refreshToken = UUID.randomUUID().toString()
        val userWithToken = user.copy(refreshToken = refreshToken)
        appUserRepository.save(userWithToken)

        val token = jwtUtil.generateToken(user.username)
        return ResponseEntity.ok().body(LoginResponse("Login successful", token, refreshToken))
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody refreshTokenRequest: RefreshTokenRequest): ResponseEntity<LoginResponse> {
        val refreshToken = refreshTokenRequest.refreshToken
        val user = appUserRepository.findByRefreshToken(refreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(LoginResponse("Invalid refresh token", null, null))

        val newToken = jwtUtil.generateToken(user.username)

        return ResponseEntity.ok()
            .body(LoginResponse("Token refreshed", newToken, null))
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
