package net.froihofer.dsfinance.bank.ejb.rest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthRestService {
    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/me")
    @RolesAllowed({"employee", "customer"})
    public Response currentUser() {
        var principal = securityContext.getUserPrincipal();
        if (principal == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String role = securityContext.isUserInRole("employee") ? "employee" : "customer";
        return Response.ok(new AuthResponse(principal.getName(), role)).build();
    }

    public record AuthResponse(String username, String role) {}
}

