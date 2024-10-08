package tech.hexd.adaptiveLearningCompanion.dependencies.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import tech.hexd.adaptiveLearningCompanion.repositories.TaskCategory
import tech.hexd.adaptiveLearningCompanion.repositories.TaskSize
import java.time.Duration

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ChatMessage(
    val role: String,
    val content: String? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAIChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @JsonProperty("max_tokens")
    val maxCompletionTokens: Int,
    val temperature: Double? = null,
    val functions: List<Map<String, Any>>? = null,
    val tools: List<ToolChoice>? = null,
    @JsonProperty("tool_choice")
    val toolChoice: Any? = null
)

data class ToolChoice(
    val type: String,
    val function: Function,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Function(
    val name: String,
    val description: String? = null,
    val parameters: Map<String, Any>? = null,
    val strict: Boolean? = false,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
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
            "required" to listOf("ownerUsername", "category", "size", "title", "listName")
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
