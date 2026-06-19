package com.cardwise.ai;

import com.cardwise.exception.AiGenerationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiProviderFactory {

    private final List<AiProvider> providers;
    private final AiProperties aiProperties;

    public AiProviderFactory(List<AiProvider> providers, AiProperties aiProperties) {
        this.providers = providers;
        this.aiProperties = aiProperties;
    }

    public AiProvider getProvider() {
        String activeProvider = aiProperties.getProvider();
        return providers.stream()
                .filter(p -> p.getProviderName().equals(activeProvider))
                .findFirst()
                .orElseThrow(() -> new AiGenerationException(
                        "No AI provider found for: " + activeProvider));
    }
}
