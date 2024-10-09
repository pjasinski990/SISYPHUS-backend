package tech.hexd.adaptiveLearningCompanion.services

import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import tech.hexd.adaptiveLearningCompanion.dependencies.OpenAIService
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.OpenAICreatedTasks
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.OpenAICreatedTasks.Companion.unravelTaskTool
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.logger
import tech.hexd.adaptiveLearningCompanion.dependencies.dto.objectMapper
import tech.hexd.adaptiveLearningCompanion.repositories.Task
import tech.hexd.adaptiveLearningCompanion.repositories.TaskRepository
import java.time.LocalDateTime

@Service
class GenerativeService(
    private val taskRepository: TaskRepository,
    private val openAIService: OpenAIService
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun unravel(taskId: String): ResponseEntity<List<Task>> {
        logger.info("Unraveling task $taskId")
        val optionalTask = taskRepository.findById(taskId)
        if (!optionalTask.isPresent) {
            return ResponseEntity.notFound().build()
        }

        val task = optionalTask.get()
        val promptMessage = """
You are an assistant that helps break down tasks into smaller, actionable steps.

Task Details:
${objectMapper.writeValueAsString(task)}

Instructions:
- Break down the above task into clear, actionable steps required to complete it.
- Ensure that the steps are specific and ordered logically.
- Only include relevant steps necessary for completing the task.
- Do not include any additional commentary or information.
- Description should be formatted with markdown.
- Start by rephrasing the task in your own words and pondering about how to best achieve it.
- Use relevant emojis in task titles to add visual flair and to make them distinguishable.
- Coding tasks should not include documentation creation, code should be self-documenting according to clean code principles.

Example:

Task Details:
{"title": "Plan a birthday party", "description": "Organize a birthday party for my friend", "category": "PINK", "size": "BIG"}

Actionable Steps:
- Choose a date and time for the party.
- Create a guest list.
- Select a venue.
- Send out invitations.
- Plan the menu and order food.
- Arrange for decorations.
- Organize entertainment or activities.
- Confirm all arrangements a day before the party.

Now, please divide the task provided above into actionable subtasks.
""".trimIndent()
        val response = openAIService.getLLMResponse(promptMessage, unravelTaskTool)
            ?: throw Exception("Error getting unravel response from LLM provider")
        val tasks = OpenAICreatedTasks.fromJson(response)?.tasks
            ?: throw Exception("Error parsing response to OpenAICreated Tasks")
        logger.info("parsed tasks: $tasks")


        val now = LocalDateTime.now()
        val newTasks = tasks.map { openAITask ->
            Task(
                ownerUsername = task.ownerUsername,
                category = openAITask.category,
                size = openAITask.size,
                title = openAITask.title,
                description = openAITask.description,
                listName = "INBOX",
                createdAt = now,
                updatedAt = now,
                dependencies = null,
                startTime = null,
                duration = openAITask.duration,
                deadline = null,
                flexibility = openAITask.flexibility,
                finishedAt = null
            )
        }
        logger.info("New tasks created by LLM: $newTasks")

        val savedTasks = taskRepository.saveAll(newTasks)
        val savedTaskIds = savedTasks.mapNotNull { it.id }

        val updatedDependencies = task.dependencies?.toMutableList() ?: mutableListOf()
        updatedDependencies.addAll(savedTaskIds)
        val updatedTask = task.copy(
            dependencies = updatedDependencies,
            updatedAt = now
        )

        // TODO  verify dependencies correct
//        taskRepository.save(updatedTask)
        return ResponseEntity.ok(savedTasks)
    }
}
