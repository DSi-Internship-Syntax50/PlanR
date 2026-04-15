package com.example.PlanR.dto;

import java.util.List;

public record GroqChatResponse(
    List<Choice> choices
) {
    public record Choice(Message message) {}
    public record Message(String content) {}
}
