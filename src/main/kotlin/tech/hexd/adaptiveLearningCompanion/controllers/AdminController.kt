package tech.hexd.adaptiveLearningCompanion.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController {

    @GetMapping("/dashboard")
    fun adminDashboard(): ResponseEntity<*> {
        logger.info("adminDashboard request")
        return ResponseEntity.ok("admin dashboard")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AdminController::class.java)
    }
}
