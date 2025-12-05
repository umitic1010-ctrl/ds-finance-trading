package net.froihofer.dsfinance.bank.ejb.rest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.froihofer.dsfinance.bank.ejb.service.BankVolumeService;
import net.froihofer.dsfinance.bank.common.dto.BankVolumeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST API Endpoint for Bank Volume Operations (Employee only)
 */
@Path("/bank")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("employee")
public class BankRestService {
    private static final Logger log = LoggerFactory.getLogger(BankRestService.class);

    @EJB
    private BankVolumeService bankVolumeService;

    /**
     * Get current bank investable volume
     * GET /api/bank/volume
     */
    @GET
    @Path("/volume")
    public Response getBankVolume() {
        try {
            log.debug("REST: Getting bank volume");
            BankVolumeDTO volume = bankVolumeService.getBankVolume();
            return Response.ok(volume).build();
        } catch (Exception e) {
            log.error("Error getting bank volume", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    /**
     * Initialize bank volume (first time setup)
     * POST /api/bank/init
     */
    @POST
    @Path("/init")
    public Response initializeBankVolume() {
        try {
            log.info("REST: Initializing bank volume");
            bankVolumeService.initializeBankVolume();
            BankVolumeDTO volume = bankVolumeService.getBankVolume();
            return Response.ok(volume).build();
        } catch (Exception e) {
            log.error("Error initializing bank volume", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to initialize bank volume: " + e.getMessage())).build();
        }
    }
}

