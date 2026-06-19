package com.cardwise.controller;

import com.cardwise.ai.AiProviderFactory;
import com.cardwise.config.JwtAuthenticationToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiProviderFactory aiProviderFactory;

    public AiController(AiProviderFactory aiProviderFactory) {
        this.aiProviderFactory = aiProviderFactory;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, List<Map<String, String>>>> generate(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal JwtAuthenticationToken auth) {

        String source = request.get("source");
        String sourceType = request.getOrDefault("sourceType", "text");

        if (source == null || source.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("cards", List.of()));
        }

        List<Map<String, String>> cards = aiProviderFactory.getProvider().generateCards(source, sourceType);
        return ResponseEntity.ok(Map.of("cards", cards));
    }
}
