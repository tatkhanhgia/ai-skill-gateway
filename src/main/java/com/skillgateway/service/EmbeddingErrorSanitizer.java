package com.skillgateway.service;

final class EmbeddingErrorSanitizer {

    private EmbeddingErrorSanitizer() {
    }

    static String summarize(RuntimeException e) {
        if (e instanceof IllegalArgumentException) {
            return e.getClass().getSimpleName();
        }

        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            return e.getClass().getSimpleName();
        }
        return e.getClass().getSimpleName() + ": " + redact(message);
    }

    private static String redact(String value) {
        return value
            .replaceAll("(?i)([?&][^=\\s]*?(?:key|token|secret|password|credential)[^=\\s]*=)[^&\\s]+", "$1<redacted>")
            .replaceAll("(?i)(bearer\\s+)[^\\s]+", "$1<redacted>")
            .replaceAll("([a-z][a-z0-9+.-]*://)[^/@\\s]+@","$1<redacted>@");
    }
}
