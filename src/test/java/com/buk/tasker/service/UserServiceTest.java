package com.buk.tasker.service;

import com.buk.tasker.model.User;
import com.buk.tasker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldReturnCurrentUser() {
        String username = "testuser";
        User user = new User(username, "pass");
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, "pass", List.of()
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        UserService service = new UserService(userRepository);
        User result = service.getCurrentUser();
        assertThat(result).isEqualTo(user);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String username = "unknown";
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, "pass", List.of()
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        UserService service = new UserService(userRepository);
        assertThatThrownBy(() -> service.getCurrentUser())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь не найден");
    }
}