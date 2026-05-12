package com.skillgateway.service.embedding;

import java.util.Locale;

public enum EmbeddingProviderType {
    OLLAMA,
    OPENAI_COMPATIBLE;

    public static EmbeddingProviderType fromConfig(String value) {
        String normalized = value == null ? "ollama" : value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "", "ollama" -> OLLAMA;
            case "openai-compatible" -> OPENAI_COMPATIBLE;
            default -> throw new EmbeddingProviderException("Unsupported embedding provider: " + value);
        };
    }
}
