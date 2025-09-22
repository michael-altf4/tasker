
package com.buk.tasker.service;

import com.buk.tasker.model.Comment;
import com.buk.tasker.model.TodoItem;
import com.buk.tasker.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository repository;

    public List<Comment> getCommentsByTodoId(Long todoId) {
        return repository.findByTodoItem_Id(todoId);
    }

    public Comment createComment(Comment comment, TodoItem todoItem) {
        comment.setTodoItem(todoItem);
        return repository.save(comment);
    }

    public Comment updateComment(Long id, String text) {
        return repository.findById(id)
                .map(comment -> {
                    comment.setText(text);
                    return repository.save(comment);
                })
                .orElse(null);
    }

    public void deleteComment(Long id) {
        repository.deleteById(id);
    }
}