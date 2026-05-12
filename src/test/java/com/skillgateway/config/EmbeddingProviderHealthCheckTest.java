package com.skillgateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.skillgateway.model.dto.EmbeddingProviderStatus;
import com.skillgateway.service.EmbeddingService;
import java.util.Map;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;

class EmbeddingProviderHealthCheckTest {

    @Test
    void healthStaysUpForValidEmbeddingConfig() {
        EmbeddingProviderHealthCheck healthCheck = new EmbeddingProviderHealthCheck();
        healthCheck.embeddingService = new StatusEmbeddingService(true, "Configured");

        HealthCheckResponse response = healthCheck.call();

        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        Map<String, Object> data = response.getData().orElseThrow();
        assertEquals(true, data.get("configured"));
    }

    @Test
    void healthGoesDownForInvalidEmbeddingConfigWithoutSecrets() {
        EmbeddingProviderHealthCheck healthCheck = new EmbeddingProviderHealthCheck();
        healthCheck.embeddingService = new StatusEmbeddingService(false, "Invalid embedding URL");

        HealthCheckResponse response = healthCheck.call();

        assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
        Map<String, Object> data = response.getData().orElseThrow();
        assertEquals(false, data.get("configured"));
        assertFalse(data.toString().contains("secret"));
    }

    private static final class StatusEmbeddingService extends EmbeddingService {
        private final boolean configured;
        private final String message;

        private StatusEmbeddingService(boolean configured, String message) {
            this.configured = configured;
            this.message = message;
        }

        @Override
        public EmbeddingProviderStatus status() {
            return new EmbeddingProviderStatus(
                "openai-compatible",
                "",
                "text-embedding-model",
                768,
                3,
                configured,
                message
            );
        }
    }
}
