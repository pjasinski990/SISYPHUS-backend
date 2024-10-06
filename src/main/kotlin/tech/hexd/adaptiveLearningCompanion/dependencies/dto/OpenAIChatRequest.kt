package tech.hexd.adaptiveLearningCompanion.dependencies.dto

data class ChatMessage(
    val role: String,
    val content: String
)

data class OpenAIChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val max_tokens: Int
)

data class ChatChoice(
    val message: ChatMessage,
    val finish_reason: String?,
    val index: Int
)
