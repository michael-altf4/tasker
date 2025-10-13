
package com.buk.tasker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Schema(description = "Comment on a todo item")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique comment identifier", example = "1")
    private Long id;

    @Schema(description = "Comment text", example = "Great idea!")
    private String text;

    @Schema(description = "Creation timestamp", example = "2025-10-05T12:30:45")
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "todo_item_id")
    private TodoItem todoItem;

    public Comment() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public TodoItem getTodoItem() { return todoItem; }
    public void setTodoItem(TodoItem todoItem) { this.todoItem = todoItem; }
}