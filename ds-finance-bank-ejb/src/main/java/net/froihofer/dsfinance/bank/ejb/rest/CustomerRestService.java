package net.froihofer.dsfinance.bank.ejb.rest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.froihofer.dsfinance.bank.ejb.service.CustomerService;
import net.froihofer.dsfinance.bank.common.dto.CustomerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * REST API Endpoint for Customer Management (Employee only)
 */
@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("employee")
public class CustomerRestService {
    private static final Logger log = LoggerFactory.getLogger(CustomerRestService.class);

    @EJB
    private CustomerService customerService;

    /**
     * Create a new customer
     * POST /api/customers
     */
    @POST
    public Response createCustomer(CustomerDTO customerDTO) {
        try {
            log.info("REST: Creating customer {} {}", customerDTO.getFirstName(), customerDTO.getLastName());

            CustomerDTO created = customerService.createCustomer(customerDTO);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (Exception e) {
            log.error("Error creating customer", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    /**
     * Get all customers
     * GET /api/customers
     */
    @GET
    public Response getAllCustomers() {
        try {
            log.debug("REST: Getting all customers");
            List<CustomerDTO> customers = customerService.getAllCustomers();
            return Response.ok(customers).build();
        } catch (Exception e) {
            log.error("Error getting customers", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    /**
     * Search customers by name
     * GET /api/customers/search?name=John
     */
    @GET
    @Path("/search")
    public Response searchCustomers(@QueryParam("name") String name) {
        try {
            log.debug("REST: Searching customers by name: {}", name);

            if (name == null || name.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Search name is required")).build();
            }

            List<CustomerDTO> customers = customerService.searchByName(name);
            return Response.ok(customers).build();
        } catch (Exception e) {
            log.error("Error searching customers", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    /**
     * Get customer by customer number
     * GET /api/customers/{customerNumber}
     */
    @GET
    @Path("/{customerNumber}")
    public Response getCustomer(@PathParam("customerNumber") String customerNumber) {
        try {
            log.debug("REST: Getting customer {}", customerNumber);

            CustomerDTO customer = customerService.findByCustomerNumber(customerNumber);
            if (customer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Customer not found")).build();
            }

            return Response.ok(customer).build();
        } catch (Exception e) {
            log.error("Error getting customer", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    /**
     * Update customer
     * PUT /api/customers/{customerNumber}
     */
    @PUT
    @Path("/{customerNumber}")
    public Response updateCustomer(@PathParam("customerNumber") String customerNumber, CustomerDTO customerDTO) {
        try {
            log.info("REST: Updating customer {}", customerNumber);
            
            CustomerDTO existing = customerService.findByCustomerNumber(customerNumber);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Customer not found")).build();
            }

            customerDTO.setCustomerNumber(customerNumber);
            CustomerDTO updated = customerService.updateCustomer(customerDTO);
            return Response.ok(updated).build();
        } catch (Exception e) {
            log.error("Error updating customer", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }

    /**
     * Delete customer
     * DELETE /api/customers/{customerNumber}
     */
    @DELETE
    @Path("/{customerNumber}")
    public Response deleteCustomer(@PathParam("customerNumber") String customerNumber) {
        try {
            log.info("REST: Deleting customer {}", customerNumber);
            
            CustomerDTO existing = customerService.findByCustomerNumber(customerNumber);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Customer not found")).build();
            }

            customerService.deleteCustomer(customerNumber);
            return Response.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting customer", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage())).build();
        }
    }
}

