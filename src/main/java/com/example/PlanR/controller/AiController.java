package com.example.PlanR.controller;

import com.example.PlanR.model.User;
import com.example.PlanR.service.UserService;
import com.example.PlanR.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final UserService userService;

    public AiController(AiService aiService, UserService userService) {
        this.aiService = aiService;
        this.userService = userService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askAi(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String query = request.get("query");
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("answer", "Please ask a question!"));
        }

        String answer = aiService.getAiResponse(user, query);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}
