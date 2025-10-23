package io.github.michael_altf4.tasker.rest.resource;
import io.github.michael_altf4.tasker.storage.model.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTaskResource {

    @NotBlank(message = "Title must not be blank")
    private String title;
    private String description;
    private Priority priority = Priority.MEDIUM;

}