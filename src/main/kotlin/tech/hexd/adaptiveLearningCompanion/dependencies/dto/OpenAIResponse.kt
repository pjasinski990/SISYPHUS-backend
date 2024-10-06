package tech.hexd.adaptiveLearningCompanion.dependencies.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAIChoice(
    val text: String,
    val index: Int,
    val logprobs: Any?,
    val finish_reason: String?
)

data class OpenAIChatResponse(
    val id: String,
    @JsonProperty("object")
    val objectType: String,
    val created: Long,
    val model: String,
    val choices: List<ChatChoice>,
    val usage: Usage
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)
