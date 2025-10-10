package com.buk.tasker.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import com.buk.tasker.model.Priority;
import com.buk.tasker.model.TodoItem;
import com.buk.tasker.model.User;
import com.buk.tasker.repository.UserRepository;
import com.buk.tasker.service.TodoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoApiTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Test
    void shouldGetTestEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/test", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private HttpHeaders getJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    @Autowired
    private UserRepository userRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private TestRestTemplate createAuthenticatedRestTemplate(String username, String password) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(new User(username, passwordEncoder.encode(password)));
        }
        return restTemplate.withBasicAuth(username, password);
    }

    @Test
    void shouldCreateAndRetrieveTodo() {
        String username = "testuser";
        String password = "password";

        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate(username, password);
        TodoItem todoToCreate = new TodoItem();
        todoToCreate.setTitle("Купить молоко");
        todoToCreate.setDescription("Обязательно вечером");
        todoToCreate.setCompleted(false);
        todoToCreate.setPriority(Priority.HIGH);
        HttpEntity<TodoItem> request = new HttpEntity<>(todoToCreate, getJsonHeaders());
        ResponseEntity<TodoItem> createResponse = authRestTemplate.postForEntity("/api/todos", request, TodoItem.class);

        assertThat(createResponse.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getId()).isNotNull();

        Long id = createResponse.getBody().getId();
        ResponseEntity<TodoItem> getResponse = authRestTemplate.exchange(
                "/api/todos/{id}",
                HttpMethod.GET,
                new HttpEntity<>(getJsonHeaders()),
                TodoItem.class,
                id
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId()).isEqualTo(id);
        assertThat(getResponse.getBody().getTitle()).isEqualTo("Купить молоко");
        assertThat(getResponse.getBody().getDescription()).isEqualTo("Обязательно вечером");
        assertThat(getResponse.getBody().isCompleted()).isFalse();
        assertThat(getResponse.getBody().getPriority()).isEqualTo(Priority.HIGH);

        LocalDateTime createdAt = getResponse.getBody().getCreatedAt();
        assertThat(createdAt).isNotNull();
        assertTrue(!createdAt.isAfter(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void shouldUpdateTodo() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");
        TodoItem todo = new TodoItem();
        todo.setTitle("Старое название");
        todo.setDescription("Старое описание");
        todo.setCompleted(false);
        todo.setPriority(Priority.LOW);

        TodoItem created = authRestTemplate.postForEntity("/api/todos", new HttpEntity<>(todo, getJsonHeaders()), TodoItem.class).getBody();
        Long id = created.getId();
        TodoItem updated = new TodoItem();
        updated.setTitle("Новое название");
        updated.setDescription("Новое описание");
        updated.setCompleted(true);
        updated.setPriority(Priority.HIGH);

        ResponseEntity<TodoItem> putResponse = authRestTemplate.exchange(
                "/api/todos/{id}", HttpMethod.PUT, new HttpEntity<>(updated, getJsonHeaders()), TodoItem.class, id
        );

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        TodoItem result = putResponse.getBody();
        assertThat(result.getTitle()).isEqualTo("Новое название");
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void shouldDeleteTodo() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");
        TodoItem todo = new TodoItem();
        todo.setTitle("Удаляемая задача");
        Long id = authRestTemplate.postForEntity("/api/todos", new HttpEntity<>(todo, getJsonHeaders()), TodoItem.class).getBody().getId();
        ResponseEntity<Void> deleteResponse = authRestTemplate.exchange(
                "/api/todos/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, id
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<String> getResponse = authRestTemplate.exchange(
                "/api/todos/{id}", HttpMethod.GET, HttpEntity.EMPTY, String.class, id
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundForNonExistentTodo() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");

        ResponseEntity<String> response = authRestTemplate.exchange(
                "/api/todos/999999", HttpMethod.GET, HttpEntity.EMPTY, String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnUnauthorizedWithoutAuth() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/todos", HttpMethod.GET, new HttpEntity<>(getJsonHeaders()), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorizedWithWrongPassword() {
        TestRestTemplate badAuth = restTemplate.withBasicAuth("testuser", "wrongpassword");
        ResponseEntity<String> response = badAuth.exchange(
                "/api/todos", HttpMethod.GET, new HttpEntity<>(getJsonHeaders()), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnEmptyListWhenNoTodos() {
        String freshUser = "freshuser_" + System.currentTimeMillis();
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate(freshUser, "password");

        ResponseEntity<String> response = authRestTemplate.exchange(
                "/api/todos", HttpMethod.GET, new HttpEntity<>(getJsonHeaders()), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("[]");
    }

    @Test
    void shouldGetTodosAsJson() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = authRestTemplate.exchange(
                "/api/todos",
                HttpMethod.GET,
                entity,
                String.class
        );

        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
        System.out.println("Body: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldReturnJsonFromTodosEndpoint() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = authRestTemplate.exchange(
                "/api/todos",
                HttpMethod.GET,
                entity,
                String.class
        );

        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
        System.out.println("Body: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("title");
    }


}