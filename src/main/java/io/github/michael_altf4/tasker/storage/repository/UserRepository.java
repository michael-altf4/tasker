package io.github.michael_altf4.tasker.storage.repository;

import io.github.michael_altf4.tasker.storage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
