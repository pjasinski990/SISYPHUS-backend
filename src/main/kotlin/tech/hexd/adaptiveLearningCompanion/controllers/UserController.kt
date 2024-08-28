package tech.hexd.adaptiveLearningCompanion.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.hexd.adaptiveLearningCompanion.repositories.AppUserRepository
import tech.hexd.adaptiveLearningCompanion.util.JwtUtil

@RestController
@RequestMapping("/user")
class UserController(
    private val appUserRepository: AppUserRepository,
) {
    @GetMapping("/profile")
    fun getUser(): ResponseEntity<*> {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name
        logger.info("profile request for $username")

        return appUserRepository.findByUsername(username)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build<Any>()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }
}
