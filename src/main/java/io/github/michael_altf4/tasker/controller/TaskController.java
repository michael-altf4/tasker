package io.github.michael_altf4.tasker.controller;

import io.github.michael_altf4.tasker.model.Task;
import io.github.michael_altf4.tasker.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Manage task items")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all tasks for current user")
    public List<Task> getAllTodos() {
        log.info("Received request to list all tasks");
        return service.getAllTasks();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get todo by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todo found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "404", description = "Todo not found or access denied")
    })
    public ResponseEntity<Task> getTodoById(
            @Parameter(description = "Todo ID", example = "1") @PathVariable Long id) {
        log.info("Received request to get todo with id='{}'", id);
        return service.getTodoById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Todo with ID {} not found or access denied", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    @Operation(summary = "Create a new todo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Todo created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class)))
    })
    public ResponseEntity<Task> createTodo(@RequestBody Task todo) {
        log.info("Received request to create todo with title='{}'", todo.getTitle());
        Task saved = service.createTask(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update todo by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todo updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "404", description = "Todo not found or access denied")
    })
    public ResponseEntity<Task> updateTodo(
            @Parameter(description = "Todo ID", example = "1") @PathVariable Long id,
            @RequestBody Task todo) {
        Task updated = service.updateTask(id, todo);
        if (updated != null) {
            log.info("Received request to update todo with title='{}'", todo.getTitle());
            return ResponseEntity.ok(updated);
        } else {
            log.warn("Attempt to update non-existent or foreign todo with ID={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete todo by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todo deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Todo not found or access denied")
    })
    public ResponseEntity<Void> deleteTodo(
            @Parameter(description = "Todo ID", example = "1") @PathVariable Long id) {
        try {
            service.deleteTask(id);
            log.info("Todo with ID={} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Error deleting todo with ID={}: {}", id, e.getMessage());
            throw e;
        }
    }
}