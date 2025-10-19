package io.github.michael_altf4.tasker.storage.model;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task priority level")
public enum Priority {
    LOW, MEDIUM, HIGH
}
