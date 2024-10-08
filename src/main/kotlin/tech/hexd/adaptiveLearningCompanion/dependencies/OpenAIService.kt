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
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.OpenAICreateTaskFunction
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.OpenAICreateTaskFunction.OpenAICreatedTask

@Service
class OpenAIService(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {

    private val logger: Logger = LoggerFactory.getLogger(OpenAIService::class.java)

    @Value("\${openai.api.key}")
    private lateinit var apiKey: String

    @Value("\${openai.api.model}")
    private lateinit var model: String

    @Value("\${openai.api.max_completion_tokens}")
    private var maxCompletionTokens: Int = 150

    private val openAIUrl = "https://api.openai.com/v1/chat/completions"

    fun getLLMResponse(inputText: String): String? {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(apiKey)
        }

        val createTaskSchema = OpenAICreateTaskFunction.createTaskSchema
        logger.info("Function Schema: $createTaskSchema")

        val requestBody = OpenAIChatRequest(
            model = model,
            messages = listOf(ChatMessage(role = "user", content = inputText)),
            maxCompletionTokens = maxCompletionTokens,
            temperature = null,
            functions = listOf(createTaskSchema),
            functionCall = "auto"
        )

        val requestBodyJson = try {
            objectMapper.writeValueAsString(requestBody)
        } catch (ex: Exception) {
            logger.error("Error serializing request body: ${ex.message}", ex)
            return null
        }

        logger.info("Request Body JSON: $requestBodyJson")

        val entity = HttpEntity(requestBodyJson, headers)

        return try {
            val response: ResponseEntity<OpenAIChatResponse> = restTemplate.exchange(
                openAIUrl,
                HttpMethod.POST,
                entity,
                OpenAIChatResponse::class.java
            )
            val openAIResponse = response.body
            if (openAIResponse != null) {
                val choice = openAIResponse.choices.firstOrNull()
                val message = choice?.message
                when {
                    message?.content != null -> {
                        logger.info("Received Content: ${message.content}")
                        message.content.trim()
                    }
                    message?.functionCall != null -> {
                        logger.info("Received Function Call: ${message.functionCall}")
                        parseFunctionCallArguments(message.functionCall.arguments)
                    }
                    else -> {
                        logger.warn("No content or function_call found in the response.")
                        null }
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

    private fun parseFunctionCallArguments(arguments: String): String? {
        return try {
            val task: OpenAICreatedTask = objectMapper.readValue(arguments, OpenAICreatedTask::class.java)
            logger.info("Parsed Task: $task")
            objectMapper.writeValueAsString(task)
        } catch (ex: Exception) {
            logger.error("Error parsing function call arguments: ${ex.message}", ex)
            null
        }
    }
}
