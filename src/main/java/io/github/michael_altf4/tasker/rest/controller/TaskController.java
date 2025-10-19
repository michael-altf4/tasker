package io.github.michael_altf4.tasker.rest.controller;

import io.github.michael_altf4.tasker.exception.ErrorResponse;
import io.github.michael_altf4.tasker.rest.resource.CreateTaskResource;
import io.github.michael_altf4.tasker.rest.resource.TaskResource;
import io.github.michael_altf4.tasker.rest.resource.UpdateTaskResource;
import io.github.michael_altf4.tasker.storage.model.Task;
import io.github.michael_altf4.tasker.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Manage task items")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;
    private final ConversionService conversionService;


    @GetMapping
    @Operation(summary = "Get all tasks for current user")
    public List<TaskResource> getAllTodos() {
        log.info("Received request to list all tasks");
        List<Task> tasks = service.getAllTasks();
        List<TaskResource> resources = new ArrayList<>();
        for (Task task : tasks) {
            resources.add(conversionService.convert(task, TaskResource.class));
        }
        return resources;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "task found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))),
            @ApiResponse(responseCode = "404", description = "task not found or access denied")
    })
    public ResponseEntity<TaskResource> getTodoById(
            @Parameter(description = "task ID", example = "1") @PathVariable Long id) {
        log.info("Received request to get task with id='{}'", id);
        return service.getTodoById(id)
                .map(task -> conversionService.convert(task, TaskResource.class))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new todo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Todo created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResource> createTodo(@Valid @RequestBody CreateTaskResource request) {
        log.info("Received request to create todo with title='{}'", request.getTitle());
        Task task = conversionService.convert(request, Task.class);
        Task saved = service.createTask(task);
        TaskResource resource = conversionService.convert(saved, TaskResource.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update todo by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todo updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))),
            @ApiResponse(responseCode = "404", description = "Todo not found or access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResource> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskResource request) {
        Task updated = service.updateTask(id, request);
        if (updated != null) {
            TaskResource resource = conversionService.convert(updated, TaskResource.class);
            log.info("Updated task ID={}", id);
            return ResponseEntity.ok(resource);
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