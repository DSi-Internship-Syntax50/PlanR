package com.example.PlanR.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        // Return the "login" Thymeleaf template located in src/main/resources/templates/login.html
        return "login";
    }
    
    // As requested earlier, make sure root endpoint works for redirect demo
    @GetMapping("/")
    public String dashboard() {
        return "dashboard"; // We will map this to a basic view later or now. We can return text via @ResponseBody for now or create a basic template.
    }
}
