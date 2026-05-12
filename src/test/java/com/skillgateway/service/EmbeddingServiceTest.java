package com.skillgateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skillgateway.model.dto.EmbeddingProviderStatus;
import com.skillgateway.service.embedding.EmbeddingProviderException;
import org.junit.jupiter.api.Test;

class EmbeddingServiceTest {

    @Test
    void statusReportsValidOpenAiCompatibleConfigWithoutQuerySecrets() {
        EmbeddingService service = new EmbeddingService();
        service.provider = "openai-compatible";
        service.embeddingUrl = "https://provider.example/v1/embeddings?api_key=secret";
        service.model = "text-embedding-model";
        service.dimension = 768;
        service.timeoutSeconds = 3;

        EmbeddingProviderStatus status = service.status();

        assertTrue(status.configured());
        assertEquals("openai-compatible", status.provider());
        assertEquals("https://provider.example/v1/embeddings", status.url());
        assertFalse(status.url().contains("secret"));
    }

    @Test
    void statusRemovesUserInfoFromUrl() {
        EmbeddingService service = new EmbeddingService();
        service.provider = "openai-compatible";
        service.embeddingUrl = "https://secret-token@provider.example/v1/embeddings";
        service.model = "text-embedding-model";
        service.dimension = 768;
        service.timeoutSeconds = 3;

        EmbeddingProviderStatus status = service.status();

        assertEquals("https://provider.example/v1/embeddings", status.url());
        assertFalse(status.url().contains("secret-token"));
    }

    @Test
    void statusDoesNotLeakRawInvalidUrl() {
        EmbeddingService service = new EmbeddingService();
        service.provider = "openai-compatible";
        service.embeddingUrl = "https://provider.example/v1/embeddings?api_key=secret value";
        service.model = "text-embedding-model";
        service.dimension = 768;
        service.timeoutSeconds = 3;

        EmbeddingProviderStatus status = service.status();

        assertFalse(status.configured());
        assertEquals("Invalid embedding URL", status.message());
        assertFalse(status.message().contains("secret"));
    }

    @Test
    void statusRejectsRelativeEmbeddingUrl() {
        EmbeddingService service = new EmbeddingService();
        service.provider = "ollama";
        service.embeddingUrl = "localhost:11434/api/embeddings";
        service.model = "nomic-embed-text";
        service.dimension = 768;
        service.timeoutSeconds = 3;

        EmbeddingProviderStatus status = service.status();

        assertFalse(status.configured());
        assertEquals("Embedding URL must be an absolute HTTP or HTTPS URL", status.message());
    }

    @Test
    void statusRejectsEmptyEmbeddingUrl() {
        EmbeddingService service = new EmbeddingService();
        service.provider = "ollama";
        service.embeddingUrl = "";
        service.model = "nomic-embed-text";
        service.dimension = 768;
        service.timeoutSeconds = 3;

        EmbeddingProviderStatus status = service.status();

        assertFalse(status.configured());
        assertEquals("Embedding URL must be an absolute HTTP or HTTPS URL", status.message());
    }

    @Test
    void statusRejectsUnsupportedEmbeddingUrlScheme() {
        EmbeddingService service = new EmbeddingService();
        service.provider = "ollama";
        service.embeddingUrl = "file:///tmp/embeddings";
        service.model = "nomic-embed-text";
        service.dimension = 768;
        service.timeoutSeconds = 3;

        EmbeddingProviderStatus status = service.status();

        assertFalse(status.configured());
        assertEquals("Embedding URL must be an absolute HTTP or HTTPS URL", status.message());
    }

    @Test
    void statusRejectsBlankEmbeddingModel() {
        EmbeddingService service = new EmbeddingService();
        service.provider = "ollama";
        service.embeddingUrl = "http://localhost:11434/api/embeddings";
        service.model = "   ";
        service.dimension = 768;
        service.timeoutSeconds = 3;

        EmbeddingProviderStatus status = service.status();

        assertFalse(status.configured());
        assertEquals("Embedding model must not be blank", status.message());
    }

    @Test
    void statusRejectsDimensionThatDoesNotMatchPgvectorSchema() {
        EmbeddingService service = new EmbeddingService();
        service.provider = "ollama";
        service.embeddingUrl = "http://localhost:11434/api/embeddings";
        service.model = "nomic-embed-text";
        service.dimension = 1536;
        service.timeoutSeconds = 3;

        EmbeddingProviderStatus status = service.status();

        assertFalse(status.configured());
        assertEquals("Embedding dimension must match pgvector schema dimension 768", status.message());
    }

    @Test
    void embedRejectsDimensionThatDoesNotMatchPgvectorSchemaBeforeProviderCall() {
        EmbeddingService service = new EmbeddingService();
        service.provider = "ollama";
        service.embeddingUrl = "http://localhost:11434/api/embeddings";
        service.model = "nomic-embed-text";
        service.dimension = 1536;
        service.timeoutSeconds = 3;

        EmbeddingProviderException error = assertThrows(EmbeddingProviderException.class, () -> service.embed("query"));

        assertEquals("Embedding dimension must match pgvector schema dimension 768", error.getMessage());
    }

    @Test
    void statusRejectsInvalidProviderConfig() {
        EmbeddingService service = new EmbeddingService();
        service.provider = "unknown";
        service.embeddingUrl = "http://localhost:11434/api/embeddings";
        service.model = "nomic-embed-text";
        service.dimension = 768;
        service.timeoutSeconds = 3;

        EmbeddingProviderStatus status = service.status();

        assertFalse(status.configured());
        assertEquals("unknown", status.provider());
    }
}
