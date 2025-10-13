package com.buk.tasker.controller;

import com.buk.tasker.model.Priority;
import com.buk.tasker.model.TodoItem;
import com.buk.tasker.repository.TodoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TodoDetailController {

    private final TodoRepository repository;

    public TodoDetailController(TodoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/todo/{id}")
    @Operation(summary = "Show todo details page")
    public String getTodoDetails(@PathVariable Long id, Model model) {
        try {
            TodoItem todo = repository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Todo not found: " + id));
            model.addAttribute("todo", todo);
            return "todo-details";
        } catch (IllegalArgumentException e) {
            log.warn("Attempt to access non-existent todo with ID={}", id);
            throw e;
        }
    }

    @PostMapping("/todo/{id}/close")
    @Operation(summary = "Mark todo as completed")
    public String closeTodo(@PathVariable Long id) {
        repository.findById(id).ifPresent(todo -> {
            todo.setCompleted(true);
            repository.save(todo);
            log.info("Todo with ID={} marked as completed", id);
        });
        return "redirect:/todo/" + id;
    }

    @PostMapping("/todo/{id}/update-description")
    @Operation(summary = "Update todo description")
    public String updateDescription(@PathVariable Long id, String description) {
        repository.findById(id).ifPresent(todo -> {
            todo.setDescription(description);
            repository.save(todo);
            log.debug("Updated description for todo ID={}", id);
        });
        return "redirect:/todo/" + id;
    }

    @PostMapping("/todo/{id}/update-priority")
    @Operation(summary = "Update todo priority")
    public String updatePriority(@PathVariable Long id, @RequestParam Priority priority) {
        repository.findById(id).ifPresent(todo -> {
            todo.setPriority(priority);
            repository.save(todo);
            log.debug("Updated priority for todo ID={} to {}", id, priority);
        });
        return "redirect:/todo/" + id;
    }

    @PostMapping("/todo/{id}/update-title")
    @Operation(summary = "Update todo title")
    public String updateTitle(@PathVariable Long id, String title) {
        repository.findById(id).ifPresent(todo -> {
            if (title != null && !title.trim().isEmpty()) {
                todo.setTitle(title.trim());
                log.debug("Updated title for todo ID={}", id);
            }
            repository.save(todo);
        });
        return "redirect:/todo/" + id;
    }
}