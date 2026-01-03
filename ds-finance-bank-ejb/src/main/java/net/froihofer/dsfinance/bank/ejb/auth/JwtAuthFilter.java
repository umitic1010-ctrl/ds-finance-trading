package net.froihofer.dsfinance.bank.ejb.auth;

import jakarta.annotation.Priority;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ALT_JWT_HEADER = "X-Auth-Token";

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private JwtService jwtService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        if (isPermitAll()) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        if (path != null && path.endsWith("auth/login")) {
            return;
        }

        String token = extractToken(requestContext);
        if (token == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("Missing JWT")
                .build());
            return;
        }

        try {
            JwtService.JwtClaims claims = jwtService.validateToken(token);
            JwtPrincipal principal = new JwtPrincipal(
                claims.personId(),
                normalizeRole(claims.role()),
                claims.customerId()
            );
            requestContext.setSecurityContext(new JwtSecurityContext(principal, isSecure(requestContext)));
        } catch (IllegalArgumentException e) {
            log.debug("JWT rejected: {}", e.getMessage());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("Invalid JWT")
                .build());
        }
    }

    private boolean isPermitAll() {
        if (resourceInfo == null) {
            return false;
        }
        if (resourceInfo.getResourceMethod() != null &&
            resourceInfo.getResourceMethod().isAnnotationPresent(PermitAll.class)) {
            return true;
        }
        return resourceInfo.getResourceClass() != null &&
            resourceInfo.getResourceClass().isAnnotationPresent(PermitAll.class);
    }

    private String extractToken(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length()).trim();
        }
        String alt = requestContext.getHeaderString(ALT_JWT_HEADER);
        return alt != null && !alt.isBlank() ? alt.trim() : null;
    }

    private boolean isSecure(ContainerRequestContext requestContext) {
        SecurityContext current = requestContext.getSecurityContext();
        return current != null && current.isSecure();
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase();
    }
}
