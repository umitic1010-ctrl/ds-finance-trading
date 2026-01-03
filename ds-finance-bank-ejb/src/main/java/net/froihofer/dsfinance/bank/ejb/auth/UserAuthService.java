package net.froihofer.dsfinance.bank.ejb.auth;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import net.froihofer.dsfinance.bank.ejb.dao.CustomerDao;
import net.froihofer.dsfinance.bank.ejb.dao.PersonDao;
import net.froihofer.dsfinance.bank.ejb.entity.Customer;
import net.froihofer.dsfinance.bank.ejb.entity.Person;

@Stateless
@PermitAll
public class UserAuthService {
    @Inject
    private PersonDao personDao;

    @Inject
    private CustomerDao customerDao;

    @Inject
    private PasswordHasher passwordHasher;

    @Inject
    private JwtService jwtService;

    public AuthResult login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        Person person = personDao.findByEmail(email.trim());
        if (person == null) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (!"ACTIVE".equalsIgnoreCase(person.getStatus())) {
            throw new IllegalStateException("Account is not active");
        }

        boolean valid = passwordHasher.verifyPassword(
            password,
            person.getPasswordHash(),
            person.getPasswordSalt(),
            person.getPasswordIterations()
        );
        if (!valid) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String role = normalizeRole(person.getRole());
        Long customerId = null;
        String customerNumber = null;
        if ("customer".equals(role)) {
            Customer customer = customerDao.findByPersonId(person.getId());
            if (customer == null) {
                throw new IllegalStateException("Customer profile is missing");
            }
            customerId = customer.getId();
            customerNumber = customer.getCustomerNumber();
        }

        try {
            String token = jwtService.generateToken(person.getId(), role, customerId, person.getEmail());
            return new AuthResult(token, role, person.getId(), person.getEmail(), customerId, customerNumber);
        } catch (JwtGenerationException e) {
            // Avoid bubbling JWT issues into the caller's transaction; return a clear error.
            throw new IllegalStateException("Failed to generate authentication token", e);
        }
    }

    public Identity lookupIdentity(Long personId) {
        Person person = personDao.findById(personId);
        if (person == null) {
            return null;
        }
        String role = normalizeRole(person.getRole());
        Long customerId = null;
        String customerNumber = null;
        if ("customer".equals(role)) {
            Customer customer = customerDao.findByPersonId(person.getId());
            if (customer != null) {
                customerId = customer.getId();
                customerNumber = customer.getCustomerNumber();
            }
        }
        return new Identity(person.getId(), person.getEmail(), role, customerId, customerNumber);
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase();
    }

    public record AuthResult(String token, String role, Long personId, String email, Long customerId, String customerNumber) {}

    public record Identity(Long personId, String email, String role, Long customerId, String customerNumber) {}
}
