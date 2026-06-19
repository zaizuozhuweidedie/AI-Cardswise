package com.cardwise.ai;

import java.util.List;
import java.util.Map;

public interface AiProvider {
    String getProviderName();
    List<Map<String, String>> generateCards(String source, String sourceType);
}
