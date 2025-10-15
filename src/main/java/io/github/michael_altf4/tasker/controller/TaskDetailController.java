package io.github.michael_altf4.tasker.controller;

import io.github.michael_altf4.tasker.model.Priority;
import io.github.michael_altf4.tasker.model.Task;
import io.github.michael_altf4.tasker.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@Controller
@Tag(name = "Todo UI", description = "Web UI for managing todo items (Thymeleaf)")
public class TaskDetailController {

    private final TaskRepository repository;

    public TaskDetailController(TaskRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/task/{id}")
    @Operation(summary = "Show todo details page")
    public String getTodoDetails(@PathVariable Long id, Model model) {
        try {
            Task todo = repository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Todo not found: " + id));
            model.addAttribute("todo", todo);
            return "task-details";
        } catch (IllegalArgumentException e) {
            log.warn("Attempt to access non-existent todo with ID={}", id);
            throw e;
        }
    }

    @PostMapping("/task/{id}/close")
    @Operation(summary = "Mark todo as completed")
    public String closeTodo(@PathVariable Long id) {
        repository.findById(id).ifPresent(todo -> {
            todo.setCompleted(true);
            repository.save(todo);
            log.info("Todo with ID={} marked as completed", id);
        });
        return "redirect:/task/" + id;
    }

    @PostMapping("/task/{id}/update-description")
    @Operation(summary = "Update task description")
    public String updateDescription(@PathVariable Long id, String description) {
        repository.findById(id).ifPresent(todo -> {
            todo.setDescription(description);
            repository.save(todo);
            log.debug("Updated description for task ID={}", id);
        });
        return "redirect:/task/" + id;
    }

    @PostMapping("/task/{id}/update-priority")
    @Operation(summary = "Update task priority")
    public String updatePriority(@PathVariable Long id, @RequestParam Priority priority) {
        repository.findById(id).ifPresent(todo -> {
            todo.setPriority(priority);
            repository.save(todo);
            log.debug("Updated priority for task ID={} to {}", id, priority);
        });
        return "redirect:/task/" + id;
    }

    @PostMapping("/task/{id}/update-title")
    @Operation(summary = "Update task title")
    public String updateTitle(@PathVariable Long id, String title) {
        repository.findById(id).ifPresent(todo -> {
            if (title != null && !title.trim().isEmpty()) {
                todo.setTitle(title.trim());
                log.debug("Updated title for task ID={}", id);
            }
            repository.save(todo);
        });
        return "redirect:/task/" + id;
    }
}