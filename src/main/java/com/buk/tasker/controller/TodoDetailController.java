package com.buk.tasker.controller;

import com.buk.tasker.model.Priority;
import com.buk.tasker.model.TodoItem;
import com.buk.tasker.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TodoDetailController {

    @Autowired
    private TodoRepository repository;

    @GetMapping("/todo/{id}")
    public String getTodoDetails(@PathVariable Long id, Model model) {
        TodoItem todo = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена: " + id));
        model.addAttribute("todo", todo);
        return "todo-details";
    }

    @PostMapping("/todo/{id}/close")
    public String closeTodo(@PathVariable Long id) {
        repository.findById(id).ifPresent(todo -> {
            todo.setCompleted(true);
            repository.save(todo);
        });
        return "redirect:/todo/" + id;
    }

    @PostMapping("/todo/{id}/update-description")
    public String updateDescription(@PathVariable Long id, String description) {
        repository.findById(id).ifPresent(todo -> {
            todo.setDescription(description);
            repository.save(todo);
        });
        return "redirect:/todo/" + id;
    }

    @PostMapping("/todo/{id}/update-priority")
    public String updatePriority(@PathVariable Long id, @RequestParam Priority priority) {
        repository.findById(id).ifPresent(todo -> {
            todo.setPriority(priority);
            repository.save(todo);
        });
        return "redirect:/todo/" + id;
    }

    @PostMapping("/todo/{id}/update-title")
    public String updateTitle(@PathVariable Long id, String title) {
        repository.findById(id).ifPresent(todo -> {
            if (title != null && !title.trim().isEmpty()) {
                todo.setTitle(title.trim());
            }
            repository.save(todo);
        });
        return "redirect:/todo/" + id;
    }
}