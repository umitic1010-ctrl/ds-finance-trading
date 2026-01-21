package net.froihofer.dsfinance.bank.ejb.service;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import net.froihofer.dsfinance.bank.common.dto.CustomerDTO;
import net.froihofer.dsfinance.bank.ejb.auth.PasswordHasher;
import net.froihofer.dsfinance.bank.ejb.dao.CustomerDao;
import net.froihofer.dsfinance.bank.ejb.dao.PersonDao;
import net.froihofer.dsfinance.bank.ejb.entity.Customer;
import net.froihofer.dsfinance.bank.ejb.entity.Person;
import org.eclipse.microprofile.config.Config;
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

    @EJB
    private PersonDao personDao;

    @EJB
    private PasswordHasher passwordHasher;

    @Inject
    private Config config;

    /**
     * Create a new customer
     */
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        log.info("Creating new customer: {} {}", customerDTO.getFirstName(), customerDTO.getLastName());

        if (customerDTO.getEmail() == null || customerDTO.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (customerDTO.getFirstName() == null || customerDTO.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (customerDTO.getLastName() == null || customerDTO.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        String password = customerDTO.getPassword();
        if (password == null || password.isBlank()) {
            password = config == null ? null : config.getOptionalValue("auth.defaultCustomerPassword", String.class).orElse(null);
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Initial password is required");
        }

        Person person = new Person();
        person.setEmail(customerDTO.getEmail());
        person.setFirstName(customerDTO.getFirstName());
        person.setLastName(customerDTO.getLastName());
        person.setRole("CUSTOMER");
        person.setStatus(customerDTO.getStatus() != null ? customerDTO.getStatus() : "ACTIVE");

        PasswordHasher.PasswordHash hash = passwordHasher.hashPassword(password);
        person.setPasswordHash(hash.hash());
        person.setPasswordSalt(hash.salt());
        person.setPasswordIterations(hash.iterations());
        personDao.persist(person);

        String customerNumber = customerDTO.getCustomerNumber();
        if (customerNumber == null || customerNumber.isBlank()) {
            customerNumber = generateCustomerNumber();
        }

        Customer customer = new Customer(
            customerNumber,
            customerDTO.getAddress()
        );
        customer.setPerson(person);
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
        if (customer.getPerson() != null) {
            dto.setFirstName(customer.getPerson().getFirstName());
            dto.setLastName(customer.getPerson().getLastName());
            dto.setEmail(customer.getPerson().getEmail());
        }
        dto.setAddress(customer.getAddress());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setCity(customer.getCity());
        dto.setCountry(customer.getCountry());
        dto.setPostalCode(customer.getPostalCode());
        dto.setStatus(customer.getStatus());
        dto.setCreatedAt(customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : null);
        return dto;
    }

    private String generateCustomerNumber() {
        String candidate;
        do {
            candidate = "CUST-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (customerDao.findByCustomerNumber(candidate) != null);
        return candidate;
    }

    /**
     * Update customer
     */
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Updating customer: {}", customerDTO.getCustomerNumber());

        if (customerDTO.getCustomerNumber() == null || customerDTO.getCustomerNumber().isBlank()) {
            throw new IllegalArgumentException("Customer number is required");
        }

        Customer customer = customerDao.findByCustomerNumber(customerDTO.getCustomerNumber());
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerDTO.getCustomerNumber());
        }

        // Update Person data
        if (customer.getPerson() != null) {
            Person person = customer.getPerson();
            if (customerDTO.getFirstName() != null) {
                person.setFirstName(customerDTO.getFirstName());
            }
            if (customerDTO.getLastName() != null) {
                person.setLastName(customerDTO.getLastName());
            }
            if (customerDTO.getEmail() != null) {
                person.setEmail(customerDTO.getEmail());
            }
            personDao.merge(person);
        }

        // Update Customer data
        if (customerDTO.getAddress() != null) {
            customer.setAddress(customerDTO.getAddress());
        }
        if (customerDTO.getPhoneNumber() != null) {
            customer.setPhoneNumber(customerDTO.getPhoneNumber());
        }
        if (customerDTO.getCity() != null) {
            customer.setCity(customerDTO.getCity());
        }
        if (customerDTO.getCountry() != null) {
            customer.setCountry(customerDTO.getCountry());
        }
        if (customerDTO.getPostalCode() != null) {
            customer.setPostalCode(customerDTO.getPostalCode());
        }
        if (customerDTO.getStatus() != null) {
            customer.setStatus(customerDTO.getStatus());
        }

        customerDao.merge(customer);

        return toDTO(customer);
    }

    /**
     * Delete customer by customer number
     */
    public void deleteCustomer(String customerNumber) {
        log.info("Deleting customer: {}", customerNumber);

        if (customerNumber == null || customerNumber.isBlank()) {
            throw new IllegalArgumentException("Customer number is required");
        }

        Customer customer = customerDao.findByCustomerNumber(customerNumber);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerNumber);
        }

        // Delete customer (cascading will handle person deletion if configured)
        customerDao.remove(customer);

        // If person should be deleted separately
        if (customer.getPerson() != null) {
            personDao.remove(customer.getPerson());
        }

        log.info("Customer {} successfully deleted", customerNumber);
    }
}
