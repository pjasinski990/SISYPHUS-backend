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
import space.hexd.sisyphusBackend.repositories.RefreshToken
import space.hexd.sisyphusBackend.repositories.RefreshTokenRepository
import space.hexd.sisyphusBackend.util.JwtUtil
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthController @Autowired constructor(
    private val appUserRepository: AppUserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
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

        val refreshTokenValue = generateRefreshToken()
        val expiryDateTime = LocalDateTime.now().plusDays(30)
        refreshTokenRepository.save(RefreshToken(token = refreshTokenValue, user = user, expiryDateTime = expiryDateTime))

        val token = jwtUtil.generateToken(user.username)
        return ResponseEntity.ok().body(LoginResponse("Login successful", token, refreshTokenValue))
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody refreshTokenRequest: RefreshTokenRequest): ResponseEntity<LoginResponse> {
        val oldRefreshToken = refreshTokenRequest.refreshToken
        val refreshToken = refreshTokenRepository.findByToken(oldRefreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(LoginResponse("Invalid refresh token", null, null))

        if (refreshToken.expiryDateTime.isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(LoginResponse("Refresh token expired", null, null))
        }

        val user = refreshToken.user
        val newJwt = jwtUtil.generateToken(user.username)
        val newRefreshTokenValue = generateRefreshToken()
        val newExpiryDateTime = LocalDateTime.now().plusDays(30)

        refreshTokenRepository.delete(refreshToken)
        refreshTokenRepository.save(RefreshToken(token = newRefreshTokenValue, user = user, expiryDateTime = newExpiryDateTime))

        return ResponseEntity.ok()
            .body(LoginResponse("Token refreshed", newJwt, newRefreshTokenValue))
    }

    private fun registerUser(username: String, password: String) {
        val appUser = AppUser(
            username = username,
            password = passwordEncoder.encode(password),
            roles = listOf("ROLE_USER")
        )
        appUserRepository.save(appUser)
    }

    private fun generateRefreshToken(): String = UUID.randomUUID().toString()
}
