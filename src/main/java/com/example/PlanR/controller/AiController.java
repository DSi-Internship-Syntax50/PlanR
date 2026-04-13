package com.example.PlanR.controller;

import com.example.PlanR.model.User;
import com.example.PlanR.repository.UserRepository;
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
    private final UserRepository userRepository;

    public AiController(AiService aiService, UserRepository userRepository) {
        this.aiService = aiService;
        this.userRepository = userRepository;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askAi(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String query = request.get("query");
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("answer", "Please ask a question!"));
        }

        String answer = aiService.getAiResponse(user, query);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}
