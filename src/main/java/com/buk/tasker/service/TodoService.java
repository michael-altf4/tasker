package com.buk.tasker.service;

import com.buk.tasker.model.TodoItem;
import com.buk.tasker.model.User;
import com.buk.tasker.repository.TodoRepository;
import com.buk.tasker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TodoService {


    private final TodoRepository repository;
    private final UserService userService;

    public TodoService(TodoRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public List<TodoItem> getAllTodos() {
        User user = userService.getCurrentUser();
        return repository.findByUser(user);
    }

    public Optional<TodoItem> getTodoById(Long id) {
        User user = userService.getCurrentUser();
        return repository.findByIdAndUser(id, user);
    }

    public TodoItem createTodo(TodoItem todo) {
        User user = userService.getCurrentUser();
        todo.setUser(user);
        return repository.save(todo);
    }

    public TodoItem updateTodo(Long id, TodoItem updatedTodo) {
        User user = userService.getCurrentUser();
        return repository.findByIdAndUser(id, user)
                .map(existing -> {
                    if (updatedTodo.getTitle() != null) {
                        existing.setTitle(updatedTodo.getTitle());
                    }
                    if (updatedTodo.getDescription() != null) {
                        existing.setDescription(updatedTodo.getDescription());
                    }
                    existing.setCompleted(updatedTodo.isCompleted());

                    if (updatedTodo.getPriority() != null) {
                        existing.setPriority(updatedTodo.getPriority());
                    }

                    return repository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Задача не найдена или доступ запрещён"));
    }

    public void deleteTodo(Long id) {
        User user = userService.getCurrentUser();
        if (!repository.existsByIdAndUser(id, user)) {
            throw new RuntimeException("Задача не найдена или доступ запрещён");
        }
        repository.deleteById(id);
    }
}