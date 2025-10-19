package io.github.michael_altf4.tasker.controller;

import io.github.michael_altf4.tasker.exception.ErrorResponse;
import io.github.michael_altf4.tasker.rest.resource.*;
import io.github.michael_altf4.tasker.storage.model.Priority;
import io.github.michael_altf4.tasker.storage.model.Task;
import io.github.michael_altf4.tasker.storage.model.User;
import io.github.michael_altf4.tasker.storage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
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
        Task taskToCreate = new Task();
        taskToCreate.setTitle("Купить молоко");
        taskToCreate.setDescription("Обязательно вечером");
        taskToCreate.setCompleted(false);
        taskToCreate.setPriority(Priority.HIGH);
        HttpEntity<Task> request = new HttpEntity<>(taskToCreate, getJsonHeaders());
        ResponseEntity<TaskResource> createResponse = authRestTemplate.postForEntity("/api/tasks", request, TaskResource.class);

        assertThat(createResponse.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getId()).isNotNull();

        Long id = createResponse.getBody().getId();
        ResponseEntity<TaskResource> getResponse = authRestTemplate.exchange(
                "/api/tasks/{id}",
                HttpMethod.GET,
                new HttpEntity<>(getJsonHeaders()),
                TaskResource.class,
                id
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId()).isEqualTo(id);
        assertThat(getResponse.getBody().getTitle()).isEqualTo("Купить молоко");
        assertThat(getResponse.getBody().getDescription()).isEqualTo("Обязательно вечером");
        assertThat(getResponse.getBody().isCompleted()).isFalse();
        assertThat(getResponse.getBody().getPriority()).isEqualTo("HIGH");

        LocalDateTime createdAt = getResponse.getBody().getCreatedAt();
        assertThat(createdAt).isNotNull();
        assertTrue(!createdAt.isAfter(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void shouldUpdateTask() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");
        Task task = new Task();
        task.setTitle("Старое название");
        task.setDescription("Старое описание");
        task.setCompleted(false);
        task.setPriority(Priority.LOW);

        TaskResource created = authRestTemplate.postForEntity("/api/tasks", new HttpEntity<>(task, getJsonHeaders()), TaskResource.class).getBody();
        Long id = created.getId();

        Task updated = new Task();
        updated.setTitle("Новое название");
        updated.setDescription("Новое описание");
        updated.setCompleted(true);
        updated.setPriority(Priority.HIGH);

        ResponseEntity<TaskResource> putResponse = authRestTemplate.exchange(
                "/api/tasks/{id}", HttpMethod.PUT, new HttpEntity<>(updated, getJsonHeaders()), TaskResource.class, id
        );

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        TaskResource result = putResponse.getBody();
        assertThat(result.getTitle()).isEqualTo("Новое название");
        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getPriority()).isEqualTo("HIGH");
    }

    @Test
    void shouldDeleteTask() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");
        Task task = new Task();
        task.setTitle("Удаляемая задача");
        Long id = authRestTemplate.postForEntity("/api/tasks", new HttpEntity<>(task, getJsonHeaders()), TaskResource.class).getBody().getId();
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

    @Test
    void shouldReturnBadRequestForEmptyTitle() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");
        CreateTaskResource task = new CreateTaskResource();
        task.setTitle("");
        task.setDescription("Описание");
        task.setPriority(Priority.HIGH);
        HttpEntity<CreateTaskResource> request = new HttpEntity<>(task, getJsonHeaders());
        ResponseEntity<ErrorResponse> response = authRestTemplate.postForEntity("/api/tasks", request, ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_TITLE");
        assertThat(response.getBody().getMessage()).isEqualTo("Title must not be blank");
    }

    @Test
    void shouldReturnBadRequestForEmptyCommentTextOnUpdate() {
        TestRestTemplate authRestTemplate = createAuthenticatedRestTemplate("testuser", "password");

        CreateTaskResource task = new CreateTaskResource();
        task.setTitle("Test Task");
        task.setDescription("Description");
        task.setPriority(Priority.MEDIUM);
        ResponseEntity<TaskResource> taskResponse = authRestTemplate.postForEntity("/api/tasks", new HttpEntity<>(task, getJsonHeaders()), TaskResource.class);
        Long taskId = taskResponse.getBody().getId();

        CreateCommentResource comment = new CreateCommentResource();
        comment.setText("Initial comment");
        ResponseEntity<CommentResource> commentResponse = authRestTemplate.postForEntity(
                "/api/comments/task/" + taskId, new HttpEntity<>(comment, getJsonHeaders()), CommentResource.class
        );
        Long commentId = commentResponse.getBody().getId();

        UpdateCommentResource updateComment = new UpdateCommentResource();
        updateComment.setText("");
        HttpHeaders headers = getJsonHeaders();
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        ResponseEntity<ErrorResponse> response = authRestTemplate.exchange(
                "/api/comments/" + commentId, HttpMethod.PATCH, new HttpEntity<>(updateComment, headers), ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_TEXT");
        assertThat(response.getBody().getMessage()).isEqualTo("must not be blank");
    }
}