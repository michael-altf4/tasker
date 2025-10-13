package com.buk.tasker.controller;

import com.buk.tasker.model.TodoItem;
import com.buk.tasker.service.TodoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/todos")
@Tag(name = "Todos", description = "Manage todo items")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all todos for current user")
    public List<TodoItem> getAllTodos() {
        log.info("Received request to list all todos");
        return service.getAllTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get todo by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todo found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoItem.class))),
            @ApiResponse(responseCode = "404", description = "Todo not found or access denied")
    })
    public ResponseEntity<TodoItem> getTodoById(
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
            @ApiResponse(responseCode = "201", description = "Todo created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoItem.class)))
    })
    public ResponseEntity<TodoItem> createTodo(@RequestBody TodoItem todo) {
        log.info("Received request to create todo with title='{}'", todo.getTitle());
        TodoItem saved = service.createTodo(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update todo by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todo updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoItem.class))),
            @ApiResponse(responseCode = "404", description = "Todo not found or access denied")
    })
    public ResponseEntity<TodoItem> updateTodo(
            @Parameter(description = "Todo ID", example = "1") @PathVariable Long id,
            @RequestBody TodoItem todo) {
        TodoItem updated = service.updateTodo(id, todo);
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
            service.deleteTodo(id);
            log.info("Todo with ID={} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Error deleting todo with ID={}: {}", id, e.getMessage());
            throw e;
        }
    }
}