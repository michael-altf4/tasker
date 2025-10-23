
package io.github.michael_altf4.tasker.service;

import io.github.michael_altf4.tasker.storage.model.Comment;
import io.github.michael_altf4.tasker.storage.model.Task;
import io.github.michael_altf4.tasker.storage.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository repository;

    public List<Comment> getCommentsByTodoId(Long todoId) {
        log.debug("Fetching comments for task ID={}", todoId);
        return repository.findByTask_Id(todoId);
    }

    public Comment createComment(Comment comment, Task task) {
        comment.setTask(task);
        Comment saved = repository.save(comment);
        log.info("Created comment ID={} for task ID={}", saved.getId(), task.getId());
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