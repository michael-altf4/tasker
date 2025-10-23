package io.github.michael_altf4.tasker.storage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString(exclude = {"comments"})
@EqualsAndHashCode(exclude = {"comments"})
@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private boolean completed;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Task priority: LOW, MEDIUM, HIGH")
    private Priority priority;

    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Task() {
        this.createdAt = LocalDateTime.now();
        this.priority = Priority.MEDIUM;
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
        this.priority = Priority.MEDIUM;
    }

    public String getPriorityLabel() {
        if (this.priority == null) return "Not set";
        return switch (this.priority) {
            case LOW -> "Low";
            case MEDIUM -> "Medium";
            case HIGH -> "High";
        };
    }
}