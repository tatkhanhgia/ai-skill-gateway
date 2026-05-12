package com.skillgateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class EmbeddingErrorSanitizerTest {

    @Test
    void summarizeDoesNotExposeMalformedUrlFromIllegalArgumentException() {
        RuntimeException error = new IllegalArgumentException(
            "Illegal character in query at index 55: https://secret-user@provider.example/v1/embeddings?api_key=secret value"
        );

        String summary = EmbeddingErrorSanitizer.summarize(error);

        assertEquals("IllegalArgumentException", summary);
        assertFalse(summary.contains("secret"));
        assertFalse(summary.contains("api_key"));
        assertFalse(summary.contains("provider.example"));
    }

    @Test
    void summarizeRedactsCredentialLikeValuesFromOtherRuntimeMessages() {
        RuntimeException error = new IllegalStateException(
            "provider failed https://user-token@provider.example/v1/embeddings?access_token=secret-token with bearer abc123"
        );

        String summary = EmbeddingErrorSanitizer.summarize(error);

        assertFalse(summary.contains("user-token"));
        assertFalse(summary.contains("secret-token"));
        assertFalse(summary.contains("abc123"));
        assertEquals(
            "IllegalStateException: provider failed https://<redacted>@provider.example/v1/embeddings?access_token=<redacted> with bearer <redacted>",
            summary
        );
    }
}
