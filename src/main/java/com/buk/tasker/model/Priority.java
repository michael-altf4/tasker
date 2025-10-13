package com.buk.tasker.model;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task priority level")
public enum Priority {
    LOW, MEDIUM, HIGH
}
