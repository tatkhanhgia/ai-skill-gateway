package com.skillgateway.service.embedding;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OllamaEmbeddingProviderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void embedsUsingOllamaPromptShape() throws Exception {
        CapturingClient client = new CapturingClient("{\"embedding\":[0.1,0.2]}");
        OllamaEmbeddingProvider provider = newProvider(client, 2);

        float[] embedding = provider.embed("log analysis");

        assertArrayEquals(new float[] {0.1f, 0.2f}, embedding);
        assertEquals("nomic-embed-text", objectMapper.readTree(client.body).path("model").asText());
        assertEquals("log analysis", objectMapper.readTree(client.body).path("prompt").asText());
        assertFalse(objectMapper.readTree(client.body).has("input"));
        assertEquals("application/json", client.headers.get("Content-Type"));
    }

    @Test
    void rejectsDimensionMismatch() {
        CapturingClient client = new CapturingClient("{\"embedding\":[0.1,0.2,0.3]}");
        OllamaEmbeddingProvider provider = newProvider(client, 2);

        EmbeddingProviderException error = assertThrows(EmbeddingProviderException.class, () -> provider.embed("query"));

        assertEquals("Embedding dimension mismatch: expected 2 but got 3", error.getMessage());
    }

    @Test
    void rejectsEmptyEmbedding() {
        CapturingClient client = new CapturingClient("{\"embedding\":[]}");
        OllamaEmbeddingProvider provider = newProvider(client, 2);

        EmbeddingProviderException error = assertThrows(EmbeddingProviderException.class, () -> provider.embed("query"));

        assertEquals("Embedding provider returned an empty embedding", error.getMessage());
    }

    @Test
    void rejectsNonNumericEmbeddingValue() {
        CapturingClient client = new CapturingClient("{\"embedding\":[0.1,\"bad\"]}");
        OllamaEmbeddingProvider provider = newProvider(client, 2);

        EmbeddingProviderException error = assertThrows(EmbeddingProviderException.class, () -> provider.embed("query"));

        assertEquals("Embedding provider returned a non-numeric embedding value", error.getMessage());
    }

    private OllamaEmbeddingProvider newProvider(CapturingClient client, int dimension) {
        return new OllamaEmbeddingProvider(
            client,
            objectMapper,
            URI.create("http://localhost:11434/api/embeddings"),
            "nomic-embed-text",
            dimension,
            Duration.ofSeconds(3)
        );
    }

    private static final class CapturingClient implements EmbeddingHttpClient {
        private final String responseBody;
        private Map<String, String> headers;
        private String body;

        private CapturingClient(String responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public EmbeddingHttpResponse post(URI uri, Duration timeout, Map<String, String> headers, String body) {
            this.headers = headers;
            this.body = body;
            return new EmbeddingHttpResponse(200, responseBody);
        }
    }
}
