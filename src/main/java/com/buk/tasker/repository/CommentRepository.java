
package com.buk.tasker.repository;

import com.buk.tasker.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTodoItem_Id(Long todoItemId);
}