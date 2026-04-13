package com.example.PlanR.dto;

import java.util.List;

public record GroqChatRequest(
    String model,
    List<Message> messages,
    double temperature
) {
    public record Message(String role, String content) {}
}
