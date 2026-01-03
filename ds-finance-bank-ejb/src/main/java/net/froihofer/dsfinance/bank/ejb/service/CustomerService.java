package net.froihofer.dsfinance.bank.ejb.service;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import net.froihofer.dsfinance.bank.common.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.ejb.dao.CustomerDao;
import net.froihofer.dsfinance.bank.ejb.entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Bean for Customer Management
 * @PermitAll allows access from REST endpoints which have their own security
 */
@Stateless
@PermitAll
public class CustomerService {
    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    @EJB
    private CustomerDao customerDao;

    /**
     * Create a new customer
     */
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        log.info("Creating new customer: {} {}", customerDTO.getFirstName(), customerDTO.getLastName());

        Customer customer = new Customer(
            customerDTO.getCustomerNumber(),
            customerDTO.getFirstName(),
            customerDTO.getLastName(),
            customerDTO.getAddress()
        );
        customer.setEmail(customerDTO.getEmail());
        customer.setPhoneNumber(customerDTO.getPhoneNumber());
        customer.setCity(customerDTO.getCity());
        customer.setCountry(customerDTO.getCountry());
        customer.setPostalCode(customerDTO.getPostalCode());
        customer.setStatus(customerDTO.getStatus());

        customerDao.persist(customer);

        return toDTO(customer);
    }

    /**
     * Find customer by customer number
     */
    public CustomerDTO findByCustomerNumber(String customerNumber) {
        log.debug("Finding customer by number: {}", customerNumber);

        Customer customer = customerDao.findByCustomerNumber(customerNumber);
        return customer == null ? null : toDTO(customer);
    }

    /**
     * Search customers by name (first or last name)
     */
    public List<CustomerDTO> searchByName(String name) {
        log.debug("Searching customers by name: {}", name);

        return customerDao.findByName(name).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get all customers
     */
    public List<CustomerDTO> getAllCustomers() {
        log.debug("Getting all customers");

        return customerDao.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get customer entity by ID (internal use)
     */
    public Customer getCustomerById(Long id) {
        return customerDao.findById(id);
    }

    /**
     * Get customer entity by customer number (internal use)
     */
    public Customer getCustomerEntityByNumber(String customerNumber) {
        return customerDao.findByCustomerNumber(customerNumber);
    }

    /**
     * Convert entity to DTO
     */
    private CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setCustomerNumber(customer.getCustomerNumber());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setAddress(customer.getAddress());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setCity(customer.getCity());
        dto.setCountry(customer.getCountry());
        dto.setPostalCode(customer.getPostalCode());
        dto.setStatus(customer.getStatus());
        dto.setCreatedAt(customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : null);
        return dto;
    }
}
