package space.hexd.sisyphusBackend.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import space.hexd.sisyphusBackend.dependencies.OpenAIService

@RestController
class OpenAIController(private val openAIService: OpenAIService) {

    @GetMapping("/generate")
    fun generateResponse(@RequestParam input: String): ResponseEntity<Any> {
        val result = openAIService.getLLMResponse(input)
        return if (result != null) {
            ResponseEntity.ok(mapOf("response" to result))
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to get response from OpenAI"))
        }
    }
}
