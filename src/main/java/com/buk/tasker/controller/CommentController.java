
package com.buk.tasker.controller;

import com.buk.tasker.model.Comment;
import com.buk.tasker.model.TodoItem;
import com.buk.tasker.service.CommentService;
import com.buk.tasker.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private TodoService todoService;

    @GetMapping("/todo/{todoId}")
    public List<Comment> getComments(@PathVariable Long todoId) {
        return commentService.getCommentsByTodoId(todoId);
    }

    @PostMapping("/todo/{todoId}")
    public ResponseEntity<Comment> addComment(@PathVariable Long todoId, @RequestBody Comment comment) {
        return todoService.getTodoById(todoId)
                .map(todoItem -> {
                    Comment saved = commentService.createComment(comment, todoItem);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id, @RequestBody String text) {
        Comment updated = commentService.updateComment(id, text);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}