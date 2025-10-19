package io.github.michael_altf4.tasker.rest.resource;
import io.github.michael_altf4.tasker.storage.model.Priority;
import jakarta.validation.constraints.NotBlank;

public class CreateTaskResource {

    @NotBlank(message = "Title must not be blank")
    private String title;
    private String description;
    private Priority priority = Priority.MEDIUM;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}