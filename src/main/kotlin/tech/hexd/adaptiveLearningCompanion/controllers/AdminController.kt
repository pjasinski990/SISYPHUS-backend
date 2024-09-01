package tech.hexd.adaptiveLearningCompanion.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController {

    @GetMapping("/dashboard")
    fun adminDashboard(): ResponseEntity<*> {
        return ResponseEntity.ok("admin dashboard")
    }
}
