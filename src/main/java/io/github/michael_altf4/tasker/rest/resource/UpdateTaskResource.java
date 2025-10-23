package io.github.michael_altf4.tasker.rest.resource;

import io.github.michael_altf4.tasker.storage.model.Priority;
import lombok.Data;

@Data
public class UpdateTaskResource {
    private String title;
    private String description;
    private Priority priority;
    private Boolean completed;

}