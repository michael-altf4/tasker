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

        // 1. Проверь, что поля не пустые
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Логин обязателен");
            return "register";
        }

        if (password == null || password.isEmpty()) {
            model.addAttribute("error", "Пароль обязателен");
            return "register";
        }

        // 2. Проверь совпадение паролей
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            model.addAttribute("username", username);
            return "register";
        }

        // 3. Проверь, что логин уникален
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Пользователь с таким логином уже существует");
            model.addAttribute("username", username);
            return "register";
        }

        // 4. Создай и сохрани пользователя
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 🔐 Хешируем!

        userRepository.save(user);

        // 5. Перенаправи на вход (можно сделать авто-вход)
        return "redirect:/login?registered=true";
    }
}