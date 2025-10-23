package io.github.michael_altf4.tasker.rest.resource;


import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TaskResource {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private String priority;
    private LocalDateTime createdAt;

}