package io.github.michael_altf4.tasker.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.github.michael_altf4.tasker.model.Priority;
import io.github.michael_altf4.tasker.model.Task;
import io.github.michael_altf4.tasker.model.User;
import io.github.michael_altf4.tasker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskApiTest {

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
    void shouldCreateAndRetrieveTask() {
        String username = "testuser";
        String password = "password";

        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate(username, password);
        Task todoToCreate = new Task();
        todoToCreate.setTitle("Купить молоко");
        todoToCreate.setDescription("Обязательно вечером");
        todoToCreate.setCompleted(false);
        todoToCreate.setPriority(Priority.HIGH);
        HttpEntity<Task> request = new HttpEntity<>(todoToCreate, getJsonHeaders());
        ResponseEntity<Task> createResponse = authRestTemplate.postForEntity("/api/tasks", request, Task.class);

        assertThat(createResponse.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getId()).isNotNull();

        Long id = createResponse.getBody().getId();
        ResponseEntity<Task> getResponse = authRestTemplate.exchange(
                "/api/tasks/{id}",
                HttpMethod.GET,
                new HttpEntity<>(getJsonHeaders()),
                Task.class,
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
    void shouldUpdateTask() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");
        Task todo = new Task();
        todo.setTitle("Старое название");
        todo.setDescription("Старое описание");
        todo.setCompleted(false);
        todo.setPriority(Priority.LOW);

        Task created = authRestTemplate.postForEntity("/api/tasks", new HttpEntity<>(todo, getJsonHeaders()), Task.class).getBody();
        Long id = created.getId();
        Task updated = new Task();
        updated.setTitle("Новое название");
        updated.setDescription("Новое описание");
        updated.setCompleted(true);
        updated.setPriority(Priority.HIGH);

        ResponseEntity<Task> putResponse = authRestTemplate.exchange(
                "/api/tasks/{id}", HttpMethod.PUT, new HttpEntity<>(updated, getJsonHeaders()), Task.class, id
        );

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Task result = putResponse.getBody();
        assertThat(result.getTitle()).isEqualTo("Новое название");
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void shouldDeleteTask() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");
        Task todo = new Task();
        todo.setTitle("Удаляемая задача");
        Long id = authRestTemplate.postForEntity("/api/tasks", new HttpEntity<>(todo, getJsonHeaders()), Task.class).getBody().getId();
        ResponseEntity<Void> deleteResponse = authRestTemplate.exchange(
                "/api/tasks/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, id
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<String> getResponse = authRestTemplate.exchange(
                "/api/tasks/{id}", HttpMethod.GET, HttpEntity.EMPTY, String.class, id
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundForNonExistentTask() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");

        ResponseEntity<String> response = authRestTemplate.exchange(
                "/api/tasks/999999", HttpMethod.GET, HttpEntity.EMPTY, String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnUnauthorizedWithoutAuth() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tasks", HttpMethod.GET, new HttpEntity<>(getJsonHeaders()), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorizedWithWrongPassword() {
        TestRestTemplate badAuth = restTemplate.withBasicAuth("testuser", "wrongpassword");
        ResponseEntity<String> response = badAuth.exchange(
                "/api/tasks", HttpMethod.GET, new HttpEntity<>(getJsonHeaders()), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnEmptyListWhenNoTasks() {
        String freshUser = "freshuser_" + System.currentTimeMillis();
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate(freshUser, "password");

        ResponseEntity<String> response = authRestTemplate.exchange(
                "/api/tasks", HttpMethod.GET, new HttpEntity<>(getJsonHeaders()), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("[]");
    }

    @Test
    void shouldGetTasksAsJson() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = authRestTemplate.exchange(
                "/api/tasks",
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
    void shouldReturnJsonFromTasksEndpoint() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = authRestTemplate.exchange(
                "/api/tasks",
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