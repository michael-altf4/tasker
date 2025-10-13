package com.buk.tasker.service;

import com.buk.tasker.model.TodoItem;
import com.buk.tasker.model.User;
import com.buk.tasker.repository.TodoRepository;
import com.buk.tasker.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
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
        log.debug("Fetching all todos for user: {}", user.getUsername());
        return repository.findByUser(user);
    }

    public Optional<TodoItem> getTodoById(Long id) {
        User user = userService.getCurrentUser();
        log.debug("Fetching todo ID={} for user: {}", id, user.getUsername());
        return repository.findByIdAndUser(id, user);
    }

    public TodoItem createTodo(TodoItem todo) {
        User user = userService.getCurrentUser();
        todo.setUser(user);
        TodoItem saved = repository.save(todo);
        log.info("Created todo ID={} with title '{}' for user: {}",
                saved.getId(), saved.getTitle(), user.getUsername());
        return saved;
    }

    public TodoItem updateTodo(Long id, TodoItem updatedTodo) {
        User user = userService.getCurrentUser();
        return repository.findByIdAndUser(id, user)
                .map(existing -> {
                    log.info("Updating todo ID={} by user: {}", id, user.getUsername());
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
                .orElseThrow(() -> {
                    log.warn("Attempt to update non-existent or foreign todo ID={} by user: {}",
                            id, user.getUsername());
                    return new RuntimeException("Todo not found or access denied");
                });
    }

    public void deleteTodo(Long id) {
        User user = userService.getCurrentUser();
        if (!repository.existsByIdAndUser(id, user)) {
            log.warn("Attempt to delete non-existent or foreign todo ID={} by user: {}",
                    id, user.getUsername());
            throw new RuntimeException("Todo not found or access denied");
        }
        repository.deleteById(id);
        log.info("Deleted todo ID={} by user: {}", id, user.getUsername());
    }
}