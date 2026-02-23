package com.skillgateway.config;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyFilter implements ContainerRequestFilter {

    @ConfigProperty(name = "auth.api-key")
    String validApiKey;

    @Override
    public void filter(ContainerRequestContext ctx) {
        if (!requiresApiKey(ctx)) {
            return;
        }

        if (validApiKey == null || validApiKey.isBlank()) {
            ctx.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"api_key_not_configured\"}")
                .build());
            return;
        }

        String apiKey = ctx.getHeaderString("X-API-Key");
        if (apiKey == null || !apiKey.equals(validApiKey)) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\":\"unauthorized\"}")
                .build());
        }
    }

    private boolean requiresApiKey(ContainerRequestContext ctx) {
        String method = ctx.getMethod();
        String path = ctx.getUriInfo().getPath();
        String normalizedPath = path != null && path.startsWith("/") ? path.substring(1) : path;

        if (!"POST".equalsIgnoreCase(method)) {
            return false;
        }
        if ("mcp".equalsIgnoreCase(normalizedPath)) {
            return true;
        }
        if ("api/v1/skills/publish".equalsIgnoreCase(normalizedPath)) {
            return true;
        }
        return normalizedPath != null && normalizedPath.contains("/versions/") && normalizedPath.endsWith("/yank");
    }
}
