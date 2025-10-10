package com.buk.tasker.controller;

import com.buk.tasker.model.User;
import com.buk.tasker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Логин обязателен");
            return "register";
        }

        if (password == null || password.isEmpty()) {
            model.addAttribute("error", "Пароль обязателен");
            return "register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            model.addAttribute("username", username);
            return "register";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Пользователь с таким логином уже существует");
            model.addAttribute("username", username);
            return "register";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
        return "redirect:/login?registered=true";
    }
}