package space.hexd.sisyphusBackend.dependencies.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.hexd.sisyphusBackend.repositories.TaskCategory
import space.hexd.sisyphusBackend.repositories.TaskSize
import java.time.Duration

val objectMapper: ObjectMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
val logger: Logger = LoggerFactory.getLogger("OpenAIChatRequest")

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
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAICreatedTask(
    val category: TaskCategory,
    val size: TaskSize,
    val title: String,
    val description: String? = null,
    val duration: Duration? = null,
    val flexibility: Float? = null,
) {
    companion object {
        val createTaskSchema: Map<String, Any> = mapOf(
            "type" to "object",
            "properties" to mapOf(
                "category" to mapOf(
                    "type" to "string",
                    "description" to "" +
                            "BLUE for domain knowledge in programming, " +
                            "GREEN for non-domain knowledge, " +
                            "YELLOW for creative work, " +
                            "PINK for social activities and relax, " +
                            "RED for health and exercise, " +
                            "WHITE for chores and household duties"
                ),
                "size" to mapOf(
                    "type" to "string",
                    "description" to "SMALL for quick tasks (less than 30 minutes), BIG for big tasks (more than 1.5 hours)"
                ),
                "title" to mapOf("type" to "string"),
                "description" to mapOf("type" to "string", "description" to "markdown format"),
                "duration" to mapOf(
                    "type" to "string",
                    "description" to "iso-8601 time, eg. PT2H"
                ),
                "flexibility" to mapOf(
                    "type" to "number",
                    "description" to "[0, 1] float. 0 is fixed task, 1 means task can be moved freely. Values between mean task can be rescheduled by n minutes where n lies between 0 and 480"
                )
            ),
            "required" to listOf("category", "size", "title", "description", "duration", "flexibility"),
        )

        private val createTaskFunction = Function(
            name = "create_task",
            description = "Create a task with the provided information.",
            parameters = createTaskSchema
        )

        private val createTaskTool = ToolChoice(
            type = "function",
            function = createTaskFunction,
        )

        fun fromJson(json: String): OpenAICreatedTask? {
            return try {
                objectMapper.readValue(json, OpenAICreatedTask::class.java)
            } catch (ex: Exception) {
                logger.error("Error parsing OpenAICreatedTask: ${ex.message}", ex)
                null
            }
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAICreatedTasks(
    val tasks: List<OpenAICreatedTask>
) {
    companion object {
        private val createTasksSchema: Map<String, Any> = mapOf(
            "type" to "object",
            "properties" to mapOf(
                "tasks" to mapOf(
                    "type" to "array",
                    "items" to OpenAICreatedTask.createTaskSchema
                )
            ),
            "required" to listOf("tasks")
        )

        private val unravelTaskFunction = Function(
            name = "unravel_task",
            description = "Create tasks that divide the provided task into actionable steps.",
            parameters = createTasksSchema
        )

        val unravelTaskTool = ToolChoice(
            type = "function",
            function = unravelTaskFunction,
        )

        fun fromJson(json: String): OpenAICreatedTasks? {
            return try {
                objectMapper.readValue(json, OpenAICreatedTasks::class.java)
            } catch (ex: Exception) {
                logger.error("Error parsing OpenAICreatedTasks: ${ex.message}", ex)
                null
            }
        }
    }
}

inline fun <reified T> parseJson(json: String): T? {
    return try {
        objectMapper.readValue(json, T::class.java)
    } catch (ex: Exception) {
        logger.error("Error parsing JSON to ${T::class.java.simpleName}: ${ex.message}", ex)
        null
    }
}
