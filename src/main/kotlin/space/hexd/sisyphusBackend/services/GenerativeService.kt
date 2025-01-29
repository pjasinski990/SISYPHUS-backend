package space.hexd.sisyphusBackend.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import space.hexd.sisyphusBackend.controllers.dto.TaskUnravelContext
import space.hexd.sisyphusBackend.dependencies.OpenAIService
import space.hexd.sisyphusBackend.dependencies.dto.OpenAICreatedTasks
import space.hexd.sisyphusBackend.dependencies.dto.OpenAICreatedTasks.Companion.unravelTaskTool
import space.hexd.sisyphusBackend.dependencies.dto.objectMapper
import space.hexd.sisyphusBackend.repositories.Task
import space.hexd.sisyphusBackend.repositories.TaskRepository
import java.time.LocalDateTime
import java.util.*

@Service
class GenerativeService(
    private val taskRepository: TaskRepository,
    private val openAIService: OpenAIService
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun unravel(context: TaskUnravelContext): ResponseEntity<List<Task>> {
        val taskId = context.taskId
        logger.info("Unraveling task $taskId")
        val optionalTask = taskRepository.findById(taskId)
        if (!optionalTask.isPresent) {
            return ResponseEntity.notFound().build()
        }

        val task = optionalTask.get()
        val projectTasks = findOtherProjectTasks(task)
        println(projectTasks)
        val promptMessage = """
You are an planning expert. You excel in breaking down tasks into smaller, actionable subtasks.

Input task:
${objectMapper.writeValueAsString(task)}

Additional context prepared for you to better understand the requirements:
${context.additionalContext}

${
    if (projectTasks.isNotEmpty()) {
        "This tag belongs to a project. Below are already existing tasks that also belong to this project. Use them for context." +
        "Take under consideration whether the task is already finished (finishedAt field set) or not. Here are the project tasks:" + 
        projectTasks.joinToString(separator = "\n") { objectMapper.writeValueAsString(it) }
    } else ""
}
        
Instructions:
- Break down the above task into clear, actionable steps required to complete it.
- Based on the complexity of the task, suggest an appropriate number of subtasks that will best divide it into actionable steps.
- Ensure that the steps are specific and ordered logically.
- Only include relevant steps necessary for completing the task.
- Do not include any additional commentary or information.
- Description should be formatted with markdown. Supported admonitions are [!WARNING] [!INFO] [!TIP] [!CAUTION] [!NOTE] [!DANGER] [!SUCCESS]
- Use relevant emojis in task titles to add visual flair.
- Coding tasks will not include documentation subtasks, code should be self-documenting according to clean code principles.
- Focus on creating actionable, measurable subtasks. Each subtask should have a specific goal or milestone to achieve.

Example 1:
Task Details:
{"title": "periodic tasks", "description": "# completion: - periodic tasks list created - tab added to left menu - periodic scheduling logic added (backend): - daily / monthly / weekly - day of week - additional scheduling fields in task creation dialog for periodic tasks", "category": "BLUE", "size": "BIG"}
[
  {
    "category": "BLUE",
    "size": "SMALL",
    "title": "Add periodic tasks tab to menu",
    "description": "New tab in the left menu created. It will be used for accessing periodic tasks."
  },
  {
    "category": "BLUE",
    "size": "BIG",
    "title": "Implement daily, weekly, monthly scheduling logic",
    "description": "Develop backend logic to handle the task scheduling \n> [!NOTE] Design choices: When implementing scheduling logic, account for edge cases like leap years, daylight saving changes, and variations in month lengths.\n> - Modularity: Keep the logic modular so it can be expanded in the future. For example, implement a strategy pattern to support additional recurrence patterns like bi-weekly or yearly.\n> - Database structure: Ensure that the MongoDB schema is optimized for periodic tasks. Consider how you'll store recurring tasks efficiently without duplicating data.\n> - Testing: Use time-based testing tools (e.g., Time Machine libraries in Kotlin) to simulate future dates and validate the scheduling over time."
  },
  {
    "category": "BLUE",
    "size": "SMALL",
    "title": "Update task creation dialog",
    "description": "Add additional scheduling fields for periodic tasks in the creation dialog\n> [!NOTE] Form Design: Use dropdowns, radio buttons, or checkboxes to help users select scheduling preferences like day of the week or frequency (e.g., “Every Monday,” “Last Friday of the month”).\n> - Validation: Ensure validation logic is robust so users can't accidentally input conflicting schedules (e.g., “Daily” + “Every Monday”).\n> - User Guidance: Add tooltips or brief descriptions near the new fields to help users understand how periodic scheduling works."
  }
]

Example 2:
Task Details:
{"title": "workout", "description": "", "category": "RED", "size": "BIG"}

Subtasks:
[
  {
    "category": "RED",
    "size": "SMALL",
    "title": "🎒 Prepare gym bag",
    "description": "Pack workout clothes, shoes, and a water bottle.",
    "duration": "PT15M",
    "flexibility": 0.8
  },
  {
    "category": "RED",
    "size": "BIG",
    "title": "🔥 Complete workout session",
    "description": "Perform scheduled exercises at the gym. \n> [!TIP] Play your favourite music.",
    "duration": "PT1H30M",
    "flexibility": 0.3
  },
  {
    "category": "RED",
    "size": "SMALL",
    "title": "🧘 Cool down and stretch",
    "description": "Do post-workout stretches to relax muscles",
    "duration": "PT15M",
    "flexibility": 0.6
  }
]

Now, please divide the task provided above into actionable subtasks.""".trimIndent()

        val response = openAIService.getLLMResponse(promptMessage, unravelTaskTool)
            ?: throw Exception("Error getting unravel response from LLM provider")
        val tasks = OpenAICreatedTasks.fromJson(response)?.tasks
            ?: throw Exception("Error parsing response to OpenAICreated Tasks")
        logger.info("parsed tasks: $tasks")


        val now = LocalDateTime.now()
        val newTasks = tasks.map { openAITask ->
            Task(
                id = UUID.randomUUID().toString(),
                ownerUsername = task.ownerUsername,
                category = openAITask.category,
                size = openAITask.size,
                title = openAITask.title,
                description = openAITask.description,
                listName = "INBOX",
                tags = extractProjectTags(task.tags ?: emptyList()),
                dependencies = null,
                startTime = null,
                duration = openAITask.duration,
                deadline = null,
                flexibility = openAITask.flexibility,
                createdAt = now,
                updatedAt = now,
                finishedAt = null
            )
        }

        logger.info("New tasks created by LLM: $newTasks")
        return ResponseEntity.ok(newTasks)
    }

    private fun findOtherProjectTasks(task: Task): List<Task> {
        if (task.tags.isNullOrEmpty()) {
            return emptyList()
        }

        val projectTags = extractProjectTags(task.tags)
        return taskRepository.findByContainsTag(projectTags).filter { it.id != task.id }
    }

    private fun extractProjectTags(tags: List<String>): List<String> {
        return tags.filter { it.startsWith("p:") }
    }
}
