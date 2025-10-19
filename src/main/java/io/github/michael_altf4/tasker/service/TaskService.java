package io.github.michael_altf4.tasker.service;

import io.github.michael_altf4.tasker.rest.resource.UpdateTaskResource;
import io.github.michael_altf4.tasker.storage.model.Task;
import io.github.michael_altf4.tasker.storage.model.User;
import io.github.michael_altf4.tasker.storage.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final UserService userService;


    public List<Task> getAllTasks() {
        User user = userService.getCurrentUser();
        log.debug("Fetching all tasks for user: {}", user.getUsername());
        return repository.findByUser(user);
    }

    public Optional<Task> getTodoById(Long id) {
        User user = userService.getCurrentUser();
        log.debug("Fetching task ID={} for user: {}", id, user.getUsername());
        return repository.findByIdAndUser(id, user);
    }

    public Task createTask(Task todo) {
        User user = userService.getCurrentUser();
        todo.setUser(user);
        Task saved = repository.save(todo);
        log.info("Created task ID={} with title '{}' for user: {}",
                saved.getId(), saved.getTitle(), user.getUsername());
        return saved;
    }

    public Task updateTask(Long id, UpdateTaskResource resource) {
        User user = userService.getCurrentUser();
        return repository.findByIdAndUser(id, user)
                .map(existing -> {
                    log.info("Updating task ID={} by user: {}", id, user.getUsername());
                    if (resource.getTitle() != null && !resource.getTitle().trim().isEmpty()) {
                        existing.setTitle(resource.getTitle().trim());
                    }
                    if (resource.getDescription() != null) {
                        existing.setDescription(resource.getDescription());
                    }
                    if (resource.getPriority() != null) {
                        existing.setPriority(resource.getPriority());
                    }
                    if (resource.isCompleted() != null) {
                        existing.setCompleted(resource.isCompleted());
                    }
                    return repository.save(existing);
                })
                .orElseThrow(() -> {
                    log.warn("Attempt to update non-existent or foreign task ID={} by user: {}",
                            id, user.getUsername());
                    return new RuntimeException("Task not found or access denied");
                });
    }

    public void deleteTask(Long id) {
        User user = userService.getCurrentUser();
        if (!repository.existsByIdAndUser(id, user)) {
            log.warn("Attempt to delete non-existent or foreign task ID={} by user: {}",
                    id, user.getUsername());
            throw new RuntimeException("Task not found or access denied");
        }
        repository.deleteById(id);
        log.info("Deleted task ID={} by user: {}", id, user.getUsername());
    }
}