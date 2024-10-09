package tech.hexd.adaptiveLearningCompanion.dependencies.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAIChatResponse(
    val id: String,
    @JsonProperty("object")
    val objectType: String,
    val created: Long,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val index: Int,
    val message: Message,
    @JsonProperty("finish_reason")
    val finishReason: String
)

data class Message(
    val role: String,
    val content: String?,
    @JsonProperty("tool_calls")
    val toolCalls: List<ToolCall>?
)

data class ToolCall(
    val id: String,
    val type: String,
    val function: CalledFunction,
)

data class CalledFunction(
    val name: String,
    val arguments: String,
)

data class Usage(
    @JsonProperty("prompt_tokens")
    val promptTokens: Int,
    @JsonProperty("completion_tokens")
    val completionTokens: Int,
    @JsonProperty("total_tokens")
    val totalTokens: Int
)
