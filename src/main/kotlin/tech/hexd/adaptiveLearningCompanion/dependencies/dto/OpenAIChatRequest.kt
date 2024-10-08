package tech.hexd.adaptiveLearningCompanion.dependencies.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ChatMessage(
    val role: String,
    val content: String? = null
)

@Serializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAIChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @JsonProperty("max_completion_tokens")
    val maxCompletionTokens: Int,
    val temperature: Double? = null,
    val functions: List<Map<String, Any>>? = null,
    @JsonProperty("function_call")
    val functionCall: String? = null
)
