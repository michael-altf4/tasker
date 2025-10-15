package io.github.michael_altf4.tasker.service;


import io.github.michael_altf4.tasker.model.Priority;
import io.github.michael_altf4.tasker.model.Task;
import io.github.michael_altf4.tasker.model.User;
import io.github.michael_altf4.tasker.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task sampleTodo;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "encodedPassword");
        sampleTodo = new Task("Купить молоко", "Срочно!");
        sampleTodo.setId(1L);
        sampleTodo.setUser(testUser);
        sampleTodo.setPriority(Priority.HIGH);
        sampleTodo.setCompleted(false);
    }

    @Test
    void shouldGetAllTasksForCurrentUser() {
        List<Task> expectedTodos = List.of(sampleTodo);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.findByUser(testUser)).thenReturn(expectedTodos);
        List<Task> result = taskService.getAllTasks();
        assertThat(result).isEqualTo(expectedTodos);
        verify(userService).getCurrentUser();
        verify(repository).findByUser(testUser);
    }

    @Test
    void shouldGetTaskByIdWhenExistsAndBelongsToUser() {
        Long todoId = 1L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.of(sampleTodo));
        Optional<Task> result = taskService.getTodoById(todoId);
        assertThat(result).contains(sampleTodo);
        verify(userService).getCurrentUser();
        verify(repository).findByIdAndUser(todoId, testUser);
    }

    @Test
    void shouldReturnEmptyOptionalWhenTaskNotFound() {
        Long todoId = 999L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.empty());
        Optional<Task> result = taskService.getTodoById(todoId);
        assertThat(result).isEmpty();
        verify(repository).findByIdAndUser(todoId, testUser);
    }

    @Test
    void shouldCreateTaskWithCurrentUser() {
        Task inputTodo = new Task("Новая задача", "Описание");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.save(any(Task.class))).thenReturn(sampleTodo);
        Task result = taskService.createTask(inputTodo);
        assertThat(result).isEqualTo(sampleTodo);
        assertThat(inputTodo.getUser()).isEqualTo(testUser);
        verify(userService).getCurrentUser();
        verify(repository).save(inputTodo);
    }

    @Test
    void shouldUpdateTaskWhenExistsAndBelongsToUser() {
        Long todoId = 1L;
        Task updatedInput = new Task();
        updatedInput.setTitle("Обновлённое название");
        updatedInput.setDescription("Новое описание");
        updatedInput.setCompleted(true);
        updatedInput.setPriority(Priority.LOW);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.of(sampleTodo));
        when(repository.save(any(Task.class))).thenReturn(sampleTodo);
        Task result = taskService.updateTask(todoId, updatedInput);
        assertThat(result.getTitle()).isEqualTo("Обновлённое название");
        assertThat(result.getDescription()).isEqualTo("Новое описание");
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getPriority()).isEqualTo(Priority.LOW);
        verify(repository).save(sampleTodo);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
        Long todoId = 999L;
        Task updatedInput = new Task();
        updatedInput.setTitle("Невозможное обновление");

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.updateTask(todoId, updatedInput))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Task not found or access denied");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteTaskWhenExistsAndBelongsToUser() {
        Long todoId = 1L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.existsByIdAndUser(todoId, testUser)).thenReturn(true);
        taskService.deleteTask(todoId);
        verify(repository).existsByIdAndUser(todoId, testUser);
        verify(repository).deleteById(todoId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        Long todoId = 999L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.existsByIdAndUser(todoId, testUser)).thenReturn(false);
        assertThatThrownBy(() -> taskService.deleteTask(todoId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Task not found or access denied");

        verify(repository, never()).deleteById(any());
    }
}