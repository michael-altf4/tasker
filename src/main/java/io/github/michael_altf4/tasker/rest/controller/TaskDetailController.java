package io.github.michael_altf4.tasker.rest.controller;

import io.github.michael_altf4.tasker.rest.resource.UpdateTaskResource;
import io.github.michael_altf4.tasker.service.TaskService;
import io.github.michael_altf4.tasker.storage.model.Priority;
import io.github.michael_altf4.tasker.storage.model.Task;
import io.github.michael_altf4.tasker.storage.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@Controller
@Tag(name = "Task UI", description = "Web UI for managing task items (Thymeleaf)")
@RequiredArgsConstructor
public class TaskDetailController {

    private final TaskService taskService;
    private final TaskRepository repository;

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

    @PatchMapping("/task/{id}")
    @Operation(summary = "Partially update task")
    public String updateTask(@PathVariable Long id, @ModelAttribute UpdateTaskResource resource) {
        taskService.updateTask(id, resource);
        log.debug("Partially updated task ID={}", id);
        return "redirect:/task/" + id;
    }

}