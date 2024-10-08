package tech.hexd.adaptiveLearningCompanion.dependencies.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import tech.hexd.adaptiveLearningCompanion.repositories.TaskCategory
import tech.hexd.adaptiveLearningCompanion.repositories.TaskSize
import java.time.Duration

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAIChatResponse(
    val id: String,
    @JsonProperty("object")
    val objectType: String,
    val created: Long,
    val choices: List<Choice>,
    val usage: Usage
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Choice(
    val index: Int,
    val message: Message,
    @JsonProperty("finish_reason")
    val finishReason: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Message(
    val role: String,
    val content: String? = null,
    @JsonProperty("function_call")
    val functionCall: FunctionCall? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FunctionCall(
    val name: String,
    val arguments: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Usage(
    @JsonProperty("prompt_tokens")
    val promptTokens: Int,
    @JsonProperty("completion_tokens")
    val completionTokens: Int,
    @JsonProperty("total_tokens")
    val totalTokens: Int
)

class OpenAICreateTaskFunction {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OpenAICreatedTask (
        val ownerUsername: String,
        val category: TaskCategory,
        val size: TaskSize,
        val title: String,
        val description: String?,
        val listName: String,
        val duration: Duration? = null,
        val dependencies: List<String>? = null,
        val flexibility: Float? = null,
    )

    companion object {
        val createTaskSchema: Map<String, Any> = mapOf(
            "name" to "create_task",
            "description" to "Create a task with the provided information.",
            "parameters" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "ownerUsername" to mapOf("type" to "string"),
                    "category" to mapOf("type" to "string"),
                    "size" to mapOf("type" to "string"),
                    "title" to mapOf("type" to "string"),
                    "description" to mapOf("type" to "string"),
                    "listName" to mapOf("type" to "string"),
                    "duration" to mapOf("type" to "string"),
                    "flexibility" to mapOf("type" to "number"),
                ),
                "required" to listOf("ownerUsername", "category")
            )
        )

        val allTaskFieldsSchema: Map<String, Any> = mapOf(
            "name" to "all_fields",
            "description" to "Create a task with the provided information.",
            "parameters" to mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "id" to mapOf("type" to "string"),
                    "ownerUsername" to mapOf("type" to "string"),
                    "category" to mapOf("type" to "string"),
                    "size" to mapOf("type" to "string"),
                    "title" to mapOf("type" to "string"),
                    "description" to mapOf("type" to "string"),
                    "listName" to mapOf("type" to "string"),
                    "startTime" to mapOf("type" to "string"),
                    "duration" to mapOf("type" to "string"),
                    "dependencies" to mapOf(
                        "type" to "array",
                        "items" to mapOf("type" to "string")
                    ),
                    "flexibility" to mapOf("type" to "number"),
                    "createdAt" to mapOf("type" to "string"),
                    "updatedAt" to mapOf("type" to "string"),
                    "finishedAt" to mapOf("type" to "string"),
                ),
                "required" to listOf("ownerUsername", "category")
            )
        )
    }
}
