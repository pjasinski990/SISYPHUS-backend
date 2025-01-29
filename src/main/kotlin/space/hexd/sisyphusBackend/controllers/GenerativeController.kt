package space.hexd.sisyphusBackend.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import space.hexd.sisyphusBackend.controllers.dto.TaskUnravelContext
import space.hexd.sisyphusBackend.repositories.Task
import space.hexd.sisyphusBackend.services.GenerativeService

@RestController
@RequestMapping("/generate")
class GenerativeController(private val generativeService: GenerativeService) {

    @PostMapping("/unravel/task")
    fun unravelTask(@RequestBody context: TaskUnravelContext): ResponseEntity<List<Task>> {
        return generativeService.unravel(context);
    }
}
