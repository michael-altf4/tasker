package com.buk.tasker.controller;

import com.buk.tasker.model.User;
import com.buk.tasker.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@Tag(name = "Authentication", description = "User registration and login (Thymeleaf UI)")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    @Operation(summary = "Show login form")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    @Operation(summary = "Show registration form")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Username is required");
            return "register";
        }

        if (password == null || password.isEmpty()) {
            model.addAttribute("error", "Password is required");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("username", username);
            return "register";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("Attempt to register existing user: {}", username);
            model.addAttribute("error", "User with this username already exists");
            model.addAttribute("username", username);
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        log.info("New user registered: {}", username);
        return "redirect:/login?registered=true";
    }
}
