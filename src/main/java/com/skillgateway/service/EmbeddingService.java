package com.skillgateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmbeddingService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ConfigProperty(name = "ai.embedding.url")
    String embeddingUrl;

    @ConfigProperty(name = "ai.embedding.model")
    String model;

    public float[] embed(String text) {
        try {
            String requestBody = objectMapper.createObjectNode()
                .put("model", model)
                .put("prompt", text)
                .toString();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(embeddingUrl))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Embedding provider failed with status " + response.statusCode());
            }

            JsonNode arr = objectMapper.readTree(response.body()).path("embedding");
            float[] out = new float[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                out[i] = (float) arr.get(i).asDouble();
            }
            return out;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Cannot create embedding", e);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create embedding", e);
        }
    }

    public String asPgVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(vector[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
