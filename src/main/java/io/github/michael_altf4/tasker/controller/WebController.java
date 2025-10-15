package io.github.michael_altf4.tasker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String showTodoPage() {
        return "tasks";
    }
}