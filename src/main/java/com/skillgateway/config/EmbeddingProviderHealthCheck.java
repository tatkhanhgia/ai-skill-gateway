package com.skillgateway.config;

import com.skillgateway.model.dto.EmbeddingProviderStatus;
import com.skillgateway.service.EmbeddingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class EmbeddingProviderHealthCheck implements HealthCheck {

    @Inject
    EmbeddingService embeddingService;

    @Override
    public HealthCheckResponse call() {
        EmbeddingProviderStatus status = embeddingService.status();
        HealthCheckResponseBuilder builder = HealthCheckResponse
            .named("embedding-provider")
            .withData("provider", status.provider())
            .withData("model", status.model() == null ? "" : status.model())
            .withData("dimension", status.dimension())
            .withData("timeoutSeconds", status.timeoutSeconds())
            .withData("message", status.message() == null ? "" : status.message());

        builder.withData("configured", status.configured());
        return (status.configured() ? builder.up() : builder.down()).build();
    }
}
