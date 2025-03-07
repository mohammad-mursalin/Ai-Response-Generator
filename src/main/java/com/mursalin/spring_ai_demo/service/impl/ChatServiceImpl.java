package com.mursalin.spring_ai_demo.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mursalin.spring_ai_demo.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    @Value("${gemini.api.key}")
    private String gemini_api_key;

    @Value("${gemini.api.url}")
    private String gemini_api_url;

    private final WebClient webClient;

    public ChatServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateReply(String message) {
        Map<String, Object> requestBody = Map.of("contents", new Object[] {
                Map.of("parts", new Object[] {
                        Map.of("text", message)})});

        String response = webClient.post()
                .uri(gemini_api_url + gemini_api_key)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractContentFromResponse(response);
    }

    private String extractContentFromResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            String content = rootNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return content;
        } catch (Exception e) {
            return "error occurred during processing : " + e.getMessage();
        }
    }
}
