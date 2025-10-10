package com.buk.tasker.repository;

import com.buk.tasker.model.TodoItem;
import com.buk.tasker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<TodoItem, Long> {
    List<TodoItem> findByUser(User user);

    Optional<TodoItem> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}