package com.skillgateway.api;

import com.skillgateway.service.ConflictException;
import com.skillgateway.service.NotFoundException;
import com.skillgateway.service.ValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof ValidationException validationException) {
            return error(Response.Status.BAD_REQUEST, "validation_error", validationException.getErrors());
        }
        if (exception instanceof NotFoundException) {
            return error(Response.Status.NOT_FOUND, "not_found", List.of(exception.getMessage()));
        }
        if (exception instanceof ConflictException) {
            return error(Response.Status.CONFLICT, "conflict", List.of(exception.getMessage()));
        }
        if (exception instanceof IllegalArgumentException) {
            return error(Response.Status.BAD_REQUEST, "invalid_argument", List.of(exception.getMessage()));
        }
        return error(Response.Status.INTERNAL_SERVER_ERROR, "internal_error", List.of("internal server error"));
    }

    private Response error(Response.Status status, String code, List<String> details) {
        return Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .entity(Map.of("error", code, "details", details))
            .build();
    }
}
