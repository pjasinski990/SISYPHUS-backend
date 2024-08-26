package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.hexd.adaptiveLearningCompanion.repositories.UserRepository
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil

@RestController
@RequestMapping("/admin")
class AdminController(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {

    @GetMapping("/dashboard")
    fun getUser(): ResponseEntity<*> {
        return ResponseEntity.ok("admin dashboard")
    }
}
