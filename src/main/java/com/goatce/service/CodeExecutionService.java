package com.goatce.service;

import com.goatce.dto.ExecuteRequest;
import com.goatce.dto.ExecuteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodeExecutionService {

    @Value("${jdoodle.client.id}")
    private String clientId;

    @Value("${jdoodle.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private static final String JDOODLE_URL = "https://api.jdoodle.com/v1/execute";

    public CodeExecutionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ExecuteResponse execute(ExecuteRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String language = mapLanguage(request.language());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("clientId", clientId);
            requestBody.put("clientSecret", clientSecret);
            requestBody.put("script", request.code());
            requestBody.put("language", language);
            requestBody.put("versionIndex", "0");
            requestBody.put("stdin", request.stdin() != null ? request.stdin() : "");

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(JDOODLE_URL, requestBody, Map.class);

            if (response == null) {
                throw new RuntimeException("Empty response from JDoodle");
            }

            String output = (String) response.get("output");
            Object statusCodeObj = response.get("statusCode");
            int statusCode = (statusCodeObj instanceof Integer) ? (Integer) statusCodeObj : Integer.parseInt(statusCodeObj.toString());

            boolean success = statusCode == 200;
            return new ExecuteResponse(
                    output,
                    !success ? output : "",
                    success ? 0 : 1,
                    success,
                    request.language(),
                    System.currentTimeMillis() - startTime
            );

        } catch (Exception e) {
            return new ExecuteResponse(
                    null,
                    e.getMessage(),
                    1,
                    false,
                    request.language(),
                    System.currentTimeMillis() - startTime
            );
        }
    }

    private String mapLanguage(String language) {
        if (language == null) return "java";
        return switch (language.toLowerCase()) {
            case "java" -> "java";
            case "python" -> "python3";
            case "javascript" -> "nodejs";
            case "c" -> "c";
            case "cpp" -> "cpp17";
            case "go" -> "go";
            default -> language.toLowerCase();
        };
    }
}
