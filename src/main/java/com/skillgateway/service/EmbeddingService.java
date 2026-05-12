package com.skillgateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillgateway.model.dto.EmbeddingProviderStatus;
import com.skillgateway.service.embedding.EmbeddingProvider;
import com.skillgateway.service.embedding.EmbeddingProviderException;
import com.skillgateway.service.embedding.EmbeddingProviderType;
import com.skillgateway.service.embedding.JdkEmbeddingHttpClient;
import com.skillgateway.service.embedding.OllamaEmbeddingProvider;
import com.skillgateway.service.embedding.OpenAiCompatibleEmbeddingProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmbeddingService {

    private static final int PGVECTOR_SCHEMA_DIMENSION = 768;

    private final JdkEmbeddingHttpClient httpClient = new JdkEmbeddingHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ConfigProperty(name = "ai.embedding.provider")
    String provider;

    @ConfigProperty(name = "ai.embedding.url")
    String embeddingUrl;

    @ConfigProperty(name = "ai.embedding.model")
    String model;

    @ConfigProperty(name = "ai.embedding.api-key")
    Optional<String> apiKey;

    @ConfigProperty(name = "ai.embedding.dimension")
    int dimension;

    @ConfigProperty(name = "ai.embedding.timeout-seconds")
    int timeoutSeconds;

    public float[] embed(String text) {
        return selectedProvider().embed(text);
    }

    public EmbeddingProviderStatus status() {
        EmbeddingProviderType type;
        try {
            type = EmbeddingProviderType.fromConfig(provider);
        } catch (RuntimeException e) {
            return new EmbeddingProviderStatus(
                provider == null || provider.isBlank() ? "ollama" : provider,
                "",
                model,
                dimension,
                timeoutSeconds,
                false,
                "Unsupported embedding provider"
            );
        }

        URI uri;
        try {
            uri = URI.create(embeddingUrl);
        } catch (RuntimeException e) {
            return status(type, null, false, "Invalid embedding URL");
        }

        if (!isSupportedHttpUri(uri)) {
            return status(type, uri, false, "Embedding URL must be an absolute HTTP or HTTPS URL");
        }
        if (model == null || model.isBlank()) {
            return status(type, uri, false, "Embedding model must not be blank");
        }
        if (dimension != PGVECTOR_SCHEMA_DIMENSION) {
            return status(type, uri, false, "Embedding dimension must match pgvector schema dimension 768");
        }
        if (timeoutSeconds <= 0) {
            return status(type, uri, false, "Embedding timeout must be positive");
        }
        return status(type, uri, true, "Embedding provider configuration is valid");
    }

    private boolean isSupportedHttpUri(URI uri) {
        String scheme = uri.getScheme();
        return uri.isAbsolute()
            && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
            && uri.getHost() != null
            && !uri.getHost().isBlank();
    }

    private EmbeddingProvider selectedProvider() {
        validateSchemaDimension();
        URI uri = URI.create(embeddingUrl);
        Duration timeout = Duration.ofSeconds(timeoutSeconds);
        EmbeddingProviderType type = EmbeddingProviderType.fromConfig(provider);
        if (type == EmbeddingProviderType.OPENAI_COMPATIBLE) {
            return new OpenAiCompatibleEmbeddingProvider(
                httpClient,
                objectMapper,
                uri,
                model,
                apiKey.orElse(""),
                dimension,
                timeout
            );
        }
        return new OllamaEmbeddingProvider(httpClient, objectMapper, uri, model, dimension, timeout);
    }

    private void validateSchemaDimension() {
        if (dimension != PGVECTOR_SCHEMA_DIMENSION) {
            throw new EmbeddingProviderException("Embedding dimension must match pgvector schema dimension 768");
        }
    }

    private EmbeddingProviderStatus status(EmbeddingProviderType type, URI uri, boolean configured, String message) {
        return new EmbeddingProviderStatus(
            type == EmbeddingProviderType.OPENAI_COMPATIBLE ? "openai-compatible" : "ollama",
            sanitizedUri(uri),
            model,
            dimension,
            timeoutSeconds,
            configured,
            message
        );
    }

    private String sanitizedUri(URI uri) {
        if (uri == null) {
            return "";
        }
        try {
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), null, null).toString();
        } catch (RuntimeException | java.net.URISyntaxException e) {
            return "";
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
