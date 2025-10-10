package com.buk.tasker.controller;

import com.buk.tasker.model.TodoItem;
import com.buk.tasker.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService service;

    @GetMapping
    public List<TodoItem> getAllTodos() {
        return service.getAllTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoItem> getTodoById(@PathVariable Long id) {
        return service.getTodoById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {

                    System.out.println("Задача с ID " + id + " не найдена");
                    return ResponseEntity.notFound().build();
                });
    }
    @PostMapping
    public TodoItem createTodo(@RequestBody TodoItem todo) {
        TodoItem saved = service.createTodo(todo);
        return saved;
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoItem> updateTodo(@PathVariable Long id, @RequestBody TodoItem todo) {
        TodoItem updated = service.updateTodo(id, todo);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        service.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }
}