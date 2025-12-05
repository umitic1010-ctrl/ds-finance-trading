package net.froihofer.dsfinance.bank.ejb.rest;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Set;

/**
 * CORS Preflight Request Filter
 * Behandelt OPTIONS requests OHNE Authentication
 * MUSS vor dem Security Filter laufen!
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 1)
public class CorsPreflightFilter implements ContainerRequestFilter {
    private static final Set<String> ALLOWED_ORIGINS = Set.of("http://localhost:3000");

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!"OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        String origin = requestContext.getHeaderString("Origin");
        if (origin == null || !ALLOWED_ORIGINS.contains(origin)) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            return;
        }

        requestContext.abortWith(
            Response.ok()
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Max-Age", "3600")
                .header("Vary", "Origin")
                .build()
        );
    }
}
