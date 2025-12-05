package net.froihofer.dsfinance.bank.ejb.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.froihofer.dsfinance.bank.ejb.rest.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global Exception Handler für REST API
 * Fängt alle unbehandelten Exceptions und gibt strukturierte Fehlermeldungen zurück
 */
@Provider
public class RestExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger log = LoggerFactory.getLogger(RestExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        log.error("REST API Exception: {}", exception.getMessage(), exception);

        // Bestimme HTTP Status basierend auf Exception-Typ
        Response.Status status;
        String message;

        if (exception instanceof IllegalArgumentException) {
            status = Response.Status.BAD_REQUEST;
            message = exception.getMessage();
        } else if (exception instanceof IllegalStateException) {
            status = Response.Status.CONFLICT;
            message = exception.getMessage();
        } else if (exception instanceof SecurityException) {
            status = Response.Status.FORBIDDEN;
            message = "Access denied: " + exception.getMessage();
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred: " + exception.getMessage();
        }

        ErrorResponse errorResponse = new ErrorResponse(message);

        return Response.status(status)
                .entity(errorResponse)
                .build();
    }
}

