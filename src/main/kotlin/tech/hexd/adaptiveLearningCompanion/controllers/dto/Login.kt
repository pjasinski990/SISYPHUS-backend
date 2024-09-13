package tech.hexd.adaptiveLearningCompanion.controllers.dto

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(val message: String, val token: String?, val refreshToken: String?)
