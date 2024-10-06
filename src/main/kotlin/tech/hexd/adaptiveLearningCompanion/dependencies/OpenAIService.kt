package tech.hexd.adaptiveLearningCompanion.dependencies

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.ChatMessage
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.OpenAIChatRequest
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.OpenAIChatResponse

@Service
class OpenAIService(private val restTemplate: RestTemplate) {

    private val logger: Logger = LoggerFactory.getLogger(OpenAIService::class.java)

    @Value("\${openai.api.key}")
    private lateinit var apiKey: String

    @Value("\${openai.api.model}")
    private lateinit var model: String

    @Value("\${openai.api.max_tokens}")
    private var maxTokens: Int = 150

    private val openAIUrl = "https://api.openai.com/v1/chat/completions"

    fun getLLMResponse(inputText: String): String? {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(apiKey)
        }

        val requestBody = OpenAIChatRequest(
            model = model,
            messages = listOf(ChatMessage(role = "user", content = inputText)),
            max_tokens = maxTokens
        )

        val entity = HttpEntity(requestBody, headers)

        return try {
            val response: ResponseEntity<OpenAIChatResponse> = restTemplate.exchange(
                openAIUrl,
                HttpMethod.POST,
                entity,
                OpenAIChatResponse::class.java
            )
            response.body?.choices?.firstOrNull()?.message?.content?.trim()
        } catch (ex: HttpClientErrorException) {
            logger.error("HTTP Error: ${ex.statusCode} - ${ex.responseBodyAsString}")
            null
        } catch (ex: Exception) {
            logger.error("Error: ${ex.message}", ex)
            null
        }
    }
}
