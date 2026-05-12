package com.skillgateway.model.dto;

public record EmbeddingProviderStatus(
    String provider,
    String url,
    String model,
    int dimension,
    int timeoutSeconds,
    boolean configured,
    String message
) {}
