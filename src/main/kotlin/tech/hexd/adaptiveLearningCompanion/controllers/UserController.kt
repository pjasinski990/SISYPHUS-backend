package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.hexd.adaptiveLearningCompanion.repositories.User
import tech.hexd.adaptiveLearningCompanion.repositories.UserRepository
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil

@RestController
@RequestMapping("/user")
class UserController(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {

    @GetMapping("/profile")
    fun getUser(): ResponseEntity<*> {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name

        return userRepository.findByUsername(username)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build<Any>()
    }
}
