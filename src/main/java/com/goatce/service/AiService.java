package com.goatce.service;

import com.goatce.dto.AiRequest;
import com.goatce.dto.AiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final RestTemplate restTemplate;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model}")
    private String model;

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AiResponse ask(AiRequest request) {
        try {
            String systemPrompt = buildSystemPrompt(request.context(), request.language());
            String userMessage = buildUserMessage(request);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userMessage));
            
            body.put("messages", messages);
            body.put("max_tokens", 1000);
            body.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", "https://goatce.app");
            headers.set("X-Title", "Goat CE");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map message = (Map) firstChoice.get("message");
                    String content = (String) message.get("content");
                    return new AiResponse(content, true, null);
                }
            }

            return new AiResponse("No response from AI.", false, "Empty response body");

        } catch (Exception e) {
            return new AiResponse("AI service unavailable.", false, e.getMessage());
        }
    }

    private String buildSystemPrompt(String context, String language) {
        StringBuilder sb = new StringBuilder("You are an expert coding assistant for Goat CE, a collaborative code editor. Be concise, helpful and practical. ");

        if ("explain".equalsIgnoreCase(context)) {
            sb.append("Explain the provided code clearly step by step. ");
        } else if ("fix".equalsIgnoreCase(context)) {
            sb.append("Find and fix bugs in the code. Show fixed code with explanation. ");
        } else if ("suggest".equalsIgnoreCase(context)) {
            sb.append("Suggest improvements for performance and best practices. ");
        } else if ("convert".equalsIgnoreCase(context)) {
            sb.append("Convert the code to the requested language maintaining the same logic. ");
        }

        if (language != null && !language.isBlank()) {
            sb.append("The code is written in ").append(language).append(". ");
        }

        return sb.toString().trim();
    }

    private String buildUserMessage(AiRequest request) {
        if (request.code() != null && !request.code().isBlank()) {
            String lang = (request.language() != null) ? request.language() : "";
            return request.prompt() + "\n\nCode:\n```" + lang + "\n" + request.code() + "\n```";
        }
        return request.prompt();
    }
}
