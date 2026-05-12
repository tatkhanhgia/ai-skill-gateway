package com.skillgateway.api;

import com.skillgateway.model.dto.EmbeddingProviderStatus;
import com.skillgateway.service.EmbeddingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/embedding")
@Produces(MediaType.APPLICATION_JSON)
public class EmbeddingStatusResource {

    @Inject
    EmbeddingService embeddingService;

    @GET
    @Path("/status")
    public EmbeddingProviderStatus status() {
        return embeddingService.status();
    }
}
