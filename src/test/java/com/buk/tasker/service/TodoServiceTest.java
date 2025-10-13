package com.buk.tasker.service;


import com.buk.tasker.model.Priority;
import com.buk.tasker.model.TodoItem;
import com.buk.tasker.model.User;
import com.buk.tasker.repository.TodoRepository;
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
class TodoServiceTest {

    @Mock
    private TodoRepository repository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TodoService todoService;

    private User testUser;
    private TodoItem sampleTodo;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "encodedPassword");
        sampleTodo = new TodoItem("Купить молоко", "Срочно!");
        sampleTodo.setId(1L);
        sampleTodo.setUser(testUser);
        sampleTodo.setPriority(Priority.HIGH);
        sampleTodo.setCompleted(false);
    }

    @Test
    void shouldGetAllTodosForCurrentUser() {
        List<TodoItem> expectedTodos = List.of(sampleTodo);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.findByUser(testUser)).thenReturn(expectedTodos);
        List<TodoItem> result = todoService.getAllTodos();
        assertThat(result).isEqualTo(expectedTodos);
        verify(userService).getCurrentUser();
        verify(repository).findByUser(testUser);
    }

    @Test
    void shouldGetTodoByIdWhenExistsAndBelongsToUser() {
        Long todoId = 1L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.of(sampleTodo));
        Optional<TodoItem> result = todoService.getTodoById(todoId);
        assertThat(result).contains(sampleTodo);
        verify(userService).getCurrentUser();
        verify(repository).findByIdAndUser(todoId, testUser);
    }

    @Test
    void shouldReturnEmptyOptionalWhenTodoNotFound() {
        Long todoId = 999L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.empty());
        Optional<TodoItem> result = todoService.getTodoById(todoId);
        assertThat(result).isEmpty();
        verify(repository).findByIdAndUser(todoId, testUser);
    }

    @Test
    void shouldCreateTodoWithCurrentUser() {
        TodoItem inputTodo = new TodoItem("Новая задача", "Описание");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.save(any(TodoItem.class))).thenReturn(sampleTodo);
        TodoItem result = todoService.createTodo(inputTodo);
        assertThat(result).isEqualTo(sampleTodo);
        assertThat(inputTodo.getUser()).isEqualTo(testUser);
        verify(userService).getCurrentUser();
        verify(repository).save(inputTodo);
    }

    @Test
    void shouldUpdateTodoWhenExistsAndBelongsToUser() {
        Long todoId = 1L;
        TodoItem updatedInput = new TodoItem();
        updatedInput.setTitle("Обновлённое название");
        updatedInput.setDescription("Новое описание");
        updatedInput.setCompleted(true);
        updatedInput.setPriority(Priority.LOW);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.of(sampleTodo));
        when(repository.save(any(TodoItem.class))).thenReturn(sampleTodo);
        TodoItem result = todoService.updateTodo(todoId, updatedInput);
        assertThat(result.getTitle()).isEqualTo("Обновлённое название");
        assertThat(result.getDescription()).isEqualTo("Новое описание");
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getPriority()).isEqualTo(Priority.LOW);
        verify(repository).save(sampleTodo);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTodo() {
        Long todoId = 999L;
        TodoItem updatedInput = new TodoItem();
        updatedInput.setTitle("Невозможное обновление");

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.findByIdAndUser(todoId, testUser)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> todoService.updateTodo(todoId, updatedInput))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Todo not found or access denied");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteTodoWhenExistsAndBelongsToUser() {
        Long todoId = 1L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.existsByIdAndUser(todoId, testUser)).thenReturn(true);
        todoService.deleteTodo(todoId);
        verify(repository).existsByIdAndUser(todoId, testUser);
        verify(repository).deleteById(todoId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTodo() {
        Long todoId = 999L;
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(repository.existsByIdAndUser(todoId, testUser)).thenReturn(false);
        assertThatThrownBy(() -> todoService.deleteTodo(todoId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Todo not found or access denied");

        verify(repository, never()).deleteById(any());
    }
}