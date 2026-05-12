package com.skillgateway.service.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

abstract class HttpEmbeddingProvider implements EmbeddingProvider {

    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_JSON = "application/json";

    protected final EmbeddingHttpClient httpClient;
    protected final ObjectMapper objectMapper;
    protected final URI uri;
    protected final String model;
    protected final int dimension;
    protected final Duration timeout;

    protected HttpEmbeddingProvider(
        EmbeddingHttpClient httpClient,
        ObjectMapper objectMapper,
        URI uri,
        String model,
        int dimension,
        Duration timeout
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.uri = uri;
        this.model = model;
        this.dimension = dimension;
        this.timeout = timeout;
    }

    protected EmbeddingHttpResponse post(Map<String, String> headers, String body) {
        try {
            EmbeddingHttpResponse response = httpClient.post(uri, timeout, headers, body);
            if (response.statusCode() >= 400) {
                throw new EmbeddingProviderException("Embedding provider failed with status " + response.statusCode());
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmbeddingProviderException("Cannot create embedding", e);
        } catch (IOException e) {
            throw new EmbeddingProviderException("Cannot create embedding", e);
        }
    }

    protected float[] parseAndValidate(JsonNode embeddingNode) {
        if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
            throw new EmbeddingProviderException("Embedding provider returned an empty embedding");
        }
        if (embeddingNode.size() != dimension) {
            throw new EmbeddingProviderException(
                "Embedding dimension mismatch: expected " + dimension + " but got " + embeddingNode.size()
            );
        }

        float[] embedding = new float[embeddingNode.size()];
        for (int i = 0; i < embeddingNode.size(); i++) {
            JsonNode value = embeddingNode.get(i);
            if (!value.isNumber()) {
                throw new EmbeddingProviderException("Embedding provider returned a non-numeric embedding value");
            }
            double asDouble = value.asDouble();
            float asFloat = (float) asDouble;
            if (!Double.isFinite(asDouble) || !Float.isFinite(asFloat)) {
                throw new EmbeddingProviderException("Embedding provider returned a non-finite embedding value");
            }
            embedding[i] = asFloat;
        }
        return embedding;
    }

    protected EmbeddingProviderException invalidResponse(IOException e) {
        return new EmbeddingProviderException("Cannot parse embedding provider response", e);
    }
}
