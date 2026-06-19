package com.cardwise.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "cardwise.ai")
public class AiProperties {
    private String provider;
    private Map<String, ProviderConfig> providers = new HashMap<>();

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Map<String, ProviderConfig> getProviders() { return providers; }
    public void setProviders(Map<String, ProviderConfig> providers) { this.providers = providers; }

    public static class ProviderConfig {
        private String apiUrl;
        private String apiKey;
        private String model;

        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }
}
