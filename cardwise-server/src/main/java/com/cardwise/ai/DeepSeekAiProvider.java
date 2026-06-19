package com.cardwise.ai;

import com.cardwise.exception.AiGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@ConditionalOnProperty(name = "cardwise.ai.provider", havingValue = "deepseek", matchIfMissing = true)
public class DeepSeekAiProvider implements AiProvider {

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are a professional Flashcard generator. Extract key concepts from the user's learning material and create question-answer flashcards.

            Each flashcard has:
            - front: A clear question or term
            - back: The corresponding answer or explanation, concise and accurate

            Requirements:
            1. Extract the most important concepts, definitions, formulas, and processes
            2. Each card focuses on one knowledge point
            3. Questions should be specific, answers should be accurate
            4. Use the same language as the source material (Chinese material -> Chinese, English material -> English)
            5. Return JSON array format: [{"front": "question", "back": "answer"}]
            6. Generate 5-15 cards depending on material length
            """;

    public DeepSeekAiProvider(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderName() {
        return "deepseek";
    }

    @Override
    public List<Map<String, String>> generateCards(String source, String sourceType) {
        AiProperties.ProviderConfig config = aiProperties.getProviders().get("deepseek");
        if (config == null || config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new AiGenerationException("DeepSeek API key is not configured");
        }

        String url = config.getApiUrl() + "/v1/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel() != null ? config.getModel() : "deepseek-chat");
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 4096);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        messages.add(Map.of("role", "user", "content", source));
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(requestBody, headers), Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("choices")) {
                throw new AiGenerationException("Empty response from AI provider");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices.isEmpty()) {
                throw new AiGenerationException("No choices in AI response");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            // Extract JSON array from response (handle markdown code blocks)
            String jsonStr = content;
            if (content.contains("```json")) {
                jsonStr = content.substring(content.indexOf("```json") + 7, content.lastIndexOf("```"));
            } else if (content.contains("```")) {
                jsonStr = content.substring(content.indexOf("```") + 3, content.lastIndexOf("```"));
            }
            jsonStr = jsonStr.trim();

            return objectMapper.readValue(jsonStr, new TypeReference<List<Map<String, String>>>() {});

        } catch (AiGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiGenerationException("Failed to generate cards: " + e.getMessage(), e);
        }
    }
}
