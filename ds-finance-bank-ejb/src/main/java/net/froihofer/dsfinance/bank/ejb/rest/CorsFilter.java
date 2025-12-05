package net.froihofer.dsfinance.bank.ejb.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.Set;

/**
 * CORS Filter für Cross-Origin Requests
 * Ermöglicht Frontend-Entwicklung auf anderem Port (z.B. React auf Port 3000)
 * Adds CORS headers to ALL responses, including error responses (401, 403, etc.)
 */
@Provider
@PreMatching
public class CorsFilter implements ContainerResponseFilter {
    private static final Set<String> ALLOWED_ORIGINS = Set.of("http://localhost:3000");

    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");

        // Only add CORS headers if origin is in allowed list
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().putSingle("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            responseContext.getHeaders().putSingle("Access-Control-Allow-Headers",
                "Authorization, Content-Type, Accept");
            responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
            responseContext.getHeaders().putSingle("Access-Control-Max-Age", "3600");
            responseContext.getHeaders().putSingle("Vary", "Origin");
        }
    }
}
