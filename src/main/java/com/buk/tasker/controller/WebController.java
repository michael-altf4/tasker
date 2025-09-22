// src/main/java/com/buk/tasker/controller/WebController.java
package com.buk.tasker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String showTodoPage() {
        return "todos"; // имя шаблона: todos.html
    }
}