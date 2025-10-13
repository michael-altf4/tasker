
package com.buk.tasker.service;

import com.buk.tasker.model.Comment;
import com.buk.tasker.model.TodoItem;
import com.buk.tasker.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CommentService {

    private final CommentRepository repository;

    public CommentService(CommentRepository repository) {
        this.repository = repository;
    }

    public List<Comment> getCommentsByTodoId(Long todoId) {
        log.debug("Fetching comments for todo ID={}", todoId);
        return repository.findByTodoItem_Id(todoId);
    }

    public Comment createComment(Comment comment, TodoItem todoItem) {
        comment.setTodoItem(todoItem);
        Comment saved = repository.save(comment);
        log.info("Created comment ID={} for todo ID={}", saved.getId(), todoItem.getId());
        return saved;
    }

    public Comment updateComment(Long id, String text) {
        return repository.findById(id)
                .map(comment -> {
                    log.info("Updated comment ID={}", id);
                    comment.setText(text);
                    return repository.save(comment);
                })
                .orElseGet(() -> {
                    log.warn("Attempt to update non-existent comment ID={}", id);
                    return null;
                });
    }

    public void deleteComment(Long id) {
        if (!repository.existsById(id)) {
            log.warn("Attempt to delete non-existent comment ID={}", id);
            return;
        }
        repository.deleteById(id);
        log.info("Deleted comment ID={}", id);
    }
}