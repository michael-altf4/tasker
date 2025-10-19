
package io.github.michael_altf4.tasker.rest.controller;

import io.github.michael_altf4.tasker.rest.resource.CommentResource;
import io.github.michael_altf4.tasker.rest.resource.CreateCommentResource;
import io.github.michael_altf4.tasker.rest.resource.UpdateCommentResource;
import io.github.michael_altf4.tasker.storage.model.Comment;
import io.github.michael_altf4.tasker.service.CommentService;
import io.github.michael_altf4.tasker.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comments", description = "Manage comments for task items")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final TaskService taskService;



    @GetMapping("/task/{todoId}")
    @Operation(summary = "Get all comments for a task item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of comments", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Task item not found")
    })
    public List<CommentResource> getComments(
            @Parameter(description = "ID of the todo item", example = "1") @PathVariable Long todoId) {
        log.debug("Fetching comments for task ID={}", todoId);
        return commentService.getCommentsByTodoId(todoId).stream()
                .map(this::toResource)
                .toList();
    }

    @PostMapping("/task/{todoId}")
    @Operation(summary = "Add a comment to a todo item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))),
            @ApiResponse(responseCode = "404", description = "Todo item not found")
    })
    public ResponseEntity<CommentResource> addComment(
            @Parameter(description = "ID of the todo item", example = "1") @PathVariable Long todoId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Comment text", required = true) @RequestBody CreateCommentResource commentResource) {
        return taskService.getTodoById(todoId)
                .map(todoItem -> {
                    Comment comment = new Comment();
                    comment.setText(commentResource.getText());
                    Comment saved = commentService.createComment(comment, todoItem);
                    log.info("Created comment ID={} for task ID={}", saved.getId(), todoId);
                    return ResponseEntity.ok(toResource(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update comment text")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<CommentResource> updateComment(
            @Parameter(description = "Comment ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New comment text", required = true) @Valid @RequestBody UpdateCommentResource commentResource) {
        Comment updated = commentService.updateComment(id, commentResource.getText());
        if (updated != null) {
            log.info("Updated comment ID={}", id);
            return ResponseEntity.ok(toResource(updated));
        } else {
            log.warn("Attempt to update non-existent comment ID={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Comment ID", example = "1") @PathVariable Long id) {
        commentService.deleteComment(id);
        log.info("Deleted comment ID={}", id);
        return ResponseEntity.noContent().build();
    }

    private CommentResource toResource(Comment comment) {
        CommentResource res = new CommentResource();
        res.setId(comment.getId());
        res.setText(comment.getText());
        res.setCreatedAt(comment.getCreatedAt());
        return res;
    }
}