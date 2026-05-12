package com.skillgateway.service.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class OpenAiCompatibleEmbeddingProvider extends HttpEmbeddingProvider {

    private final String apiKey;

    public OpenAiCompatibleEmbeddingProvider(
        EmbeddingHttpClient httpClient,
        ObjectMapper objectMapper,
        URI uri,
        String model,
        String apiKey,
        int dimension,
        Duration timeout
    ) {
        super(httpClient, objectMapper, uri, model, dimension, timeout);
        this.apiKey = apiKey;
    }

    @Override
    public float[] embed(String text) {
        String body = objectMapper.createObjectNode()
            .put("model", model)
            .put("input", text)
            .toString();
        EmbeddingHttpResponse response = post(headers(), body);
        try {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode embedding = root.path("data").path(0).path("embedding");
            return parseAndValidate(embedding);
        } catch (IOException e) {
            throw invalidResponse(e);
        }
    }

    private Map<String, String> headers() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(CONTENT_TYPE, APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.put("Authorization", "Bearer " + apiKey);
        }
        return headers;
    }
}
