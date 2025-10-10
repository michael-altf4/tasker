package com.buk.tasker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.buk.tasker.model.User;

@Entity
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private boolean completed;
    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDateTime createdAt;
    @JsonIgnore
    @OneToMany(mappedBy = "todoItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public TodoItem() {
        this.createdAt = LocalDateTime.now();
        this.priority = Priority.MEDIUM;
    }

    public TodoItem(String title, String description) {
        this.title = title;
        this.description = description;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
        this.priority = Priority.MEDIUM;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        if (priority != null) {
            this.priority = priority;
        }
    }

    public String getPriorityLabel() {
        if (this.priority == null) {
            return "не задан";
        }
        return switch (this.priority) {
            case LOW -> "Низкий";
            case MEDIUM -> "Средний";
            case HIGH -> "Высокий";
        };
    }
}

