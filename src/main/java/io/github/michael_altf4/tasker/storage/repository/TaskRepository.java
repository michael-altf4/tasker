package io.github.michael_altf4.tasker.storage.repository;

import io.github.michael_altf4.tasker.storage.model.Task;
import io.github.michael_altf4.tasker.storage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);

    Optional<Task> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}