package com.buk.tasker.repository;

import com.buk.tasker.model.TodoItem;
import com.buk.tasker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<TodoItem, Long> {
    // Найти все задачи пользователя
    List<TodoItem> findByUser(User user);

    // Найти задачу по ID и пользователю
    Optional<TodoItem> findByIdAndUser(Long id, User user);

    // Проверить существование
    boolean existsByIdAndUser(Long id, User user);
}