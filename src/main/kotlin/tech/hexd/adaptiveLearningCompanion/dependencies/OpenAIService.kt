package tech.hexd.adaptiveLearningCompanion.dependencies

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.ChatMessage
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.OpenAIChatRequest
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.OpenAIChatResponse
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.ToolChoice

@Service
class OpenAIService(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {

    private val logger: Logger = LoggerFactory.getLogger("OpenAIService")

    @Value("\${openai.api.key}")
    private lateinit var apiKey: String

    @Value("\${openai.api.model}")
    private lateinit var model: String

    @Value("\${openai.api.max_tokens}")
    private var maxCompletionTokens: Int = 1000

    private val openAIUrl = "https://api.openai.com/v1/chat/completions"

    fun getLLMResponse(modelInput: String, tool: ToolChoice? = null): String? {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(apiKey)
        }

        val requestBody: OpenAIChatRequest;
        if (tool != null) {
            requestBody = OpenAIChatRequest(
                model = model,
                messages = listOf(ChatMessage(role = "user", content = modelInput)),
                maxCompletionTokens = maxCompletionTokens,
                temperature = null,
                tools = listOf(tool),
                toolChoice = tool,
            )
        } else {
            requestBody = OpenAIChatRequest(
                model = model,
                messages = listOf(ChatMessage(role = "user", content = modelInput)),
                maxCompletionTokens = maxCompletionTokens,
                temperature = null,
                toolChoice = "null"
            )
        }

        val requestBodyJson = try {
            objectMapper.writeValueAsString(requestBody)
        } catch (ex: Exception) {
            logger.error("Error serializing request body: ${ex.message}", ex)
            return null
        }
        logger.debug("Request body JSON: $requestBodyJson")

        val entity = HttpEntity(requestBodyJson, headers)
        return try {
            val response: ResponseEntity<OpenAIChatResponse> = restTemplate.exchange(
                openAIUrl,
                HttpMethod.POST,
                entity,
                OpenAIChatResponse::class.java
            )
            val openAIResponse = response.body
            logger.debug("OpenAI API Response: {}", response.body)

            if (openAIResponse != null) {
                val choice = openAIResponse.choices.firstOrNull()
                val message = choice?.message

                when {
                    message?.content != null -> {
                        logger.info("Received Content: ${message.content}")
                        message.content.trim()
                    }
                    message?.toolCalls != null -> {
                        logger.info("Received Tool Calls: ${message.toolCalls}")
                        return if (message.toolCalls.firstOrNull() != null) {
                            message.toolCalls.first().function.arguments
                        } else ""
                    }
                    else -> {
                        logger.warn("No content or tools call found in the response.")
                        null
                    }
                }
            } else {
                logger.warn("Response body is null.")
                null
            }
        } catch (ex: HttpClientErrorException) {
            logger.error("HTTP Error: ${ex.statusCode} - ${ex.responseBodyAsString}")
            null
        } catch (ex: Exception) {
            logger.error("Error: ${ex.message}", ex)
            null
        }
    }
}
