package net.froihofer.dsfinance.bank.ejb.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import net.froihofer.dsfinance.bank.ejb.auth.UserAuthService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthRestService {
    @Context
    private SecurityContext securityContext;

    @EJB
    private UserAuthService userAuthService;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response login(AuthLoginRequest request) {
        if (request == null || request.email() == null || request.password() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Email and password are required"))
                .build();
        }
        try {
            UserAuthService.AuthResult result = userAuthService.login(request.email(), request.password());
            return Response.ok(new AuthLoginResponse(
                result.token(),
                result.role(),
                result.personId(),
                result.email(),
                result.customerId(),
                result.customerNumber()
            )).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/me")
    public Response currentUser() {
        var principal = securityContext.getUserPrincipal();
        if (principal == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Long personId = null;
        try {
            personId = Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        UserAuthService.Identity identity = userAuthService.lookupIdentity(personId);
        if (identity == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok(new AuthResponse(
            identity.email(),
            identity.role(),
            identity.personId(),
            identity.customerId(),
            identity.customerNumber()
        )).build();
    }

    public record AuthLoginRequest(String email, String password) {}
    public record AuthLoginResponse(String token, String role, Long personId, String email, Long customerId, String customerNumber) {}
    public record AuthResponse(String email, String role, Long personId, Long customerId, String customerNumber) {}
}

