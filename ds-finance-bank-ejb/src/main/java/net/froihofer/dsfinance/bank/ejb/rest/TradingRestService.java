package net.froihofer.dsfinance.bank.ejb.rest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import net.froihofer.dsfinance.bank.ejb.service.BankFacadeService;
import net.froihofer.dsfinance.bank.common.dto.DepotDTO;
import net.froihofer.dsfinance.bank.common.dto.StockDTO;
import net.froihofer.dsfinance.bank.common.dto.TradeRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * REST API Endpoint for Trading Operations
 * Accessible by both employees and customers
 */
@Path("/trading")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"employee", "customer"})
public class TradingRestService {
    private static final Logger log = LoggerFactory.getLogger(TradingRestService.class);

    @EJB
    private BankFacadeService bankFacadeService;

    @Context
    private SecurityContext securityContext;

    /**
     * Search for stocks
     * GET /api/trading/stocks/search?query=Apple
     */
    @GET
    @Path("/stocks/search")
    public Response searchStocks(@QueryParam("query") String query) {
        try {
            log.debug("REST: Searching stocks with query: {}", query);

            if (query == null || query.trim().isEmpty()) {
                query = ""; // Return all or handle accordingly
            }

            List<StockDTO> stocks = bankFacadeService.searchStocks(query);
            return Response.ok(stocks).build();
        } catch (Exception e) {
            log.error("Error searching stocks", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    /**
     * Get depot for a customer
     * GET /api/trading/depot/{customerNumber}
     *
     * Employees can access any customer's depot
     * Customers can only access their own depot
     */
    @GET
    @Path("/depot/{customerNumber}")
    public Response getDepot(@PathParam("customerNumber") String customerNumber) {
        try {
            log.debug("REST: Getting depot for customer: {}", customerNumber);

            // Security check: customers can only access their own depot
            if (securityContext.isUserInRole("customer")) {
                String username = securityContext.getUserPrincipal().getName();
                if (!username.equals(customerNumber)) {
                    return Response.status(Response.Status.FORBIDDEN)
                            .entity(new ErrorResponse("You can only access your own depot")).build();
                }
            }

            DepotDTO depot = bankFacadeService.getDepot(customerNumber);
            return Response.ok(depot).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (Exception e) {
            log.error("Error getting depot", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    /**
     * Buy stocks
     * POST /api/trading/buy
     *
     * Employees specify customerNumber in request body
     * Customers automatically use their own account
     */
    @POST
    @Path("/buy")
    public Response buyStocks(TradeRequestDTO request) {
        try {
            String customerNumber = request.getCustomerNumber();

            // If customer, override customerNumber with authenticated user
            if (securityContext.isUserInRole("customer")) {
                customerNumber = securityContext.getUserPrincipal().getName();
            }

            if (customerNumber == null || customerNumber.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Customer number is required")).build();
            }

            log.info("REST: Buy request - {} shares of {} for customer {}",
                    request.getQuantity(), request.getStockSymbol(), customerNumber);

            bankFacadeService.buyStocks(customerNumber, request.getStockSymbol(), request.getQuantity());

            return Response.ok(new SuccessResponse("Successfully bought " + request.getQuantity() +
                    " shares of " + request.getStockSymbol())).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (Exception e) {
            log.error("Error buying stocks", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    /**
     * Sell stocks
     * POST /api/trading/sell
     *
     * Employees specify customerNumber in request body
     * Customers automatically use their own account
     */
    @POST
    @Path("/sell")
    public Response sellStocks(TradeRequestDTO request) {
        try {
            String customerNumber = request.getCustomerNumber();

            // If customer, override customerNumber with authenticated user
            if (securityContext.isUserInRole("customer")) {
                customerNumber = securityContext.getUserPrincipal().getName();
            }

            if (customerNumber == null || customerNumber.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Customer number is required")).build();
            }

            log.info("REST: Sell request - {} shares of {} for customer {}",
                    request.getQuantity(), request.getStockSymbol(), customerNumber);

            bankFacadeService.sellStocks(customerNumber, request.getStockSymbol(), request.getQuantity());

            return Response.ok(new SuccessResponse("Successfully sold " + request.getQuantity() +
                    " shares of " + request.getStockSymbol())).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage())).build();
        } catch (Exception e) {
            log.error("Error selling stocks", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }
}

