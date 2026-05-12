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

class OpenAiCompatibleEmbeddingProviderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void embedsUsingOpenAiCompatibleInputShape() throws Exception {
        CapturingClient client = new CapturingClient("{\"data\":[{\"embedding\":[0.1,0.2]}]}");
        OpenAiCompatibleEmbeddingProvider provider = newProvider(client, "test-key", 2);

        float[] embedding = provider.embed("semantic query");

        assertArrayEquals(new float[] {0.1f, 0.2f}, embedding);
        assertEquals("text-embedding-3-small", objectMapper.readTree(client.body).path("model").asText());
        assertEquals("semantic query", objectMapper.readTree(client.body).path("input").asText());
        assertFalse(objectMapper.readTree(client.body).has("prompt"));
        assertEquals("application/json", client.headers.get("Content-Type"));
        assertEquals("Bearer test-key", client.headers.get("Authorization"));
    }

    @Test
    void skipsAuthorizationHeaderWhenApiKeyBlank() {
        CapturingClient client = new CapturingClient("{\"data\":[{\"embedding\":[0.1,0.2]}]}");
        OpenAiCompatibleEmbeddingProvider provider = newProvider(client, " ", 2);

        provider.embed("query");

        assertFalse(client.headers.containsKey("Authorization"));
    }

    @Test
    void rejectsMissingDataWithoutLeakingApiKey() {
        CapturingClient client = new CapturingClient("{\"data\":[]}");
        OpenAiCompatibleEmbeddingProvider provider = newProvider(client, "secret-test-key", 2);

        EmbeddingProviderException error = assertThrows(EmbeddingProviderException.class, () -> provider.embed("query"));

        assertEquals("Embedding provider returned an empty embedding", error.getMessage());
        assertFalse(error.getMessage().contains("secret-test-key"));
    }

    @Test
    void rejectsProviderHttpError() {
        CapturingClient client = new CapturingClient("{}", 401);
        OpenAiCompatibleEmbeddingProvider provider = newProvider(client, "secret-test-key", 2);

        EmbeddingProviderException error = assertThrows(EmbeddingProviderException.class, () -> provider.embed("query"));

        assertEquals("Embedding provider failed with status 401", error.getMessage());
        assertFalse(error.getMessage().contains("secret-test-key"));
    }

    @Test
    void rejectsNonFiniteEmbeddingValue() {
        CapturingClient client = new CapturingClient("{\"data\":[{\"embedding\":[1.0E100,0.2]}]}");
        OpenAiCompatibleEmbeddingProvider provider = newProvider(client, "test-key", 2);

        EmbeddingProviderException error = assertThrows(EmbeddingProviderException.class, () -> provider.embed("query"));

        assertEquals("Embedding provider returned a non-finite embedding value", error.getMessage());
    }

    private OpenAiCompatibleEmbeddingProvider newProvider(CapturingClient client, String apiKey, int dimension) {
        return new OpenAiCompatibleEmbeddingProvider(
            client,
            objectMapper,
            URI.create("http://localhost:8080/v1/embeddings"),
            "text-embedding-3-small",
            apiKey,
            dimension,
            Duration.ofSeconds(3)
        );
    }

    private static final class CapturingClient implements EmbeddingHttpClient {
        private final String responseBody;
        private final int statusCode;
        private Map<String, String> headers;
        private String body;

        private CapturingClient(String responseBody) {
            this(responseBody, 200);
        }

        private CapturingClient(String responseBody, int statusCode) {
            this.responseBody = responseBody;
            this.statusCode = statusCode;
        }

        @Override
        public EmbeddingHttpResponse post(URI uri, Duration timeout, Map<String, String> headers, String body) {
            this.headers = headers;
            this.body = body;
            return new EmbeddingHttpResponse(statusCode, responseBody);
        }
    }
}
