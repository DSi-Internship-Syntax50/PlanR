package com.example.PlanR.service;

import com.example.PlanR.dto.GroqChatRequest;
import com.example.PlanR.dto.GroqChatResponse;
import com.example.PlanR.model.MasterRoutine;
import com.example.PlanR.model.User;
import com.example.PlanR.repository.MasterRoutineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final MasterRoutineRepository routineRepository;
    private final RestTemplate restTemplate;

    public AiService(MasterRoutineRepository routineRepository, RestTemplate restTemplate) {
        this.routineRepository = routineRepository;
        this.restTemplate = restTemplate;
    }

    public String getAiResponse(User user, String userQuery) {
        try {
            String context = buildRoutineContext(user);
            
            // Debugging log (visible in console)
            logger.info("Calling Groq API for user: {} with context length: {}", user.getEmail(), context.length());
            if (apiKey == null || apiKey.equals("your_api_key_here") || apiKey.isEmpty()) {
                logger.error("CRITICAL: Groq API Key is missing or using placeholder in application.properties!");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemPrompt =
"You are PlanR AI, the official AI assistant exclusively for the PlanR University Scheduling & Management System.\n\n" +

"YOUR ROLE:\n" +
"- ONLY answer questions related to the PlanR platform.\n" +
"- Topics include: class schedules, routines, room assignments, courses, faculty, seat plans, event bookings, batch info, and platform usage.\n\n" +

"STRICT DOMAIN RULE:\n" +
"- If a question is NOT related to PlanR, you MUST refuse.\n" +
"- Respond ONLY with:\n" +
"  \"I can only assist with PlanR-related topics (schedule, routines, rooms, events, etc.). Please use a general-purpose tool for other queries.\"\n" +
"- Do NOT explain further. Do NOT attempt to answer.\n\n" +

"RESPONSE STYLE (VERY IMPORTANT):\n" +
"- ALWAYS give SHORT, DIRECT answers.\n" +
"- ALWAYS use BULLET POINTS.\n" +
"- NO paragraphs.\n" +
"- NO extra explanations unless explicitly asked.\n" +
"- NO greetings, NO filler text.\n" +
"- Keep answers minimal and to the point.\n\n" +

"CONTEXT:\n" +
"- User Batch: " + user.getCurrentBatch() + "\n\n" +

"### CURRENT USER'S ROUTINE DATA ###\n" +
context;

            GroqChatRequest request = new GroqChatRequest(
                model,
                List.of(
                    new GroqChatRequest.Message("system", systemPrompt),
                    new GroqChatRequest.Message("user", userQuery)
                ),
                0.7
            );

            HttpEntity<GroqChatRequest> entity = new HttpEntity<>(request, headers);
            GroqChatResponse response = restTemplate.postForObject(apiUrl, entity, GroqChatResponse.class);

            if (response != null && !response.choices().isEmpty()) {
                return response.choices().get(0).message().content();
            }

            return "I'm having trouble connecting to my brain right now. Please try again!";
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            logger.error("Groq API Error: Status {} - Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "AI API Error: " + e.getStatusCode() + ". Check console for details.";
        } catch (Exception e) {
            logger.error("Unexpected Error during AI query", e);
            return "I encountered an error while processing your request. Check console logs.";
        }
    }

    private String buildRoutineContext(User user) {
        String batch = user.getCurrentBatch();
        if (batch == null) return "No routine data available for your batch.";

        List<MasterRoutine> routines = routineRepository.findByCourseBatch(batch);
        
        if (routines.isEmpty()) return "No classes scheduled for your batch (" + batch + ").";

        return routines.stream()
                .map(r -> String.format("- %s: %s at %s in %s (Section %s)",
                        r.getDayOfWeek(),
                        r.getCourse().getTitle(),
                        r.getStartTime(),
                        r.getRoom().getRoomNumber(),
                        r.getSection()))
                .collect(Collectors.joining("\n"));
    }
}
