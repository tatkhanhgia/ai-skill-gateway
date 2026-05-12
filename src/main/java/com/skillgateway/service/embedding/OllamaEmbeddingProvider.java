package com.skillgateway.service.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

public class OllamaEmbeddingProvider extends HttpEmbeddingProvider {

    public OllamaEmbeddingProvider(
        EmbeddingHttpClient httpClient,
        ObjectMapper objectMapper,
        URI uri,
        String model,
        int dimension,
        Duration timeout
    ) {
        super(httpClient, objectMapper, uri, model, dimension, timeout);
    }

    @Override
    public float[] embed(String text) {
        String body = objectMapper.createObjectNode()
            .put("model", model)
            .put("prompt", text)
            .toString();
        EmbeddingHttpResponse response = post(Map.of(CONTENT_TYPE, APPLICATION_JSON), body);
        try {
            JsonNode root = objectMapper.readTree(response.body());
            return parseAndValidate(root.path("embedding"));
        } catch (IOException e) {
            throw invalidResponse(e);
        }
    }
}
