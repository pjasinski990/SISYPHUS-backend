package tech.hexd.adaptiveLearningCompanion.util

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class ResponseForger {
    private var status: HttpStatus = HttpStatus.OK
    private val responseBody: MutableMap<String, Any> = mutableMapOf()

    fun ok(message: String? = null): ResponseForger {
        status = HttpStatus.OK
        responseBody["success"] = true
        if (message != null) {
            responseBody["message"] = message
        }
        return this
    }

    fun badRequestFailure(message: String? = null): ResponseForger {
        status = HttpStatus.BAD_REQUEST
        responseBody["success"] = false
        if (message != null) {
            responseBody["message"] = message
        }
        return this
    }

    fun withField(key: String, value: Any): ResponseForger {
        responseBody[key] = value
        return this
    }

    fun build(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.status(status).body(responseBody)
    }
}