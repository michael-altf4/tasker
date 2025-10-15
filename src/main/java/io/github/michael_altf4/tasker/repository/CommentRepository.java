
package io.github.michael_altf4.tasker.repository;

import io.github.michael_altf4.tasker.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTask_Id(Long todoItemId);
}