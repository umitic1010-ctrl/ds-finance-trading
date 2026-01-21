package net.froihofer.dsfinance.bank.ejb.dao;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import net.froihofer.dsfinance.bank.ejb.entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Stateless
@PermitAll
public class CustomerDao {
    private static final Logger log = LoggerFactory.getLogger(CustomerDao.class);

    @PersistenceContext(unitName = "ds-finance-bank-ref-persunit")
    private EntityManager em;

    public Customer persist(Customer customer) {
        log.debug("Persisting customer {}", customer != null ? customer.getCustomerNumber() : "null");
        em.persist(customer);
        return customer;
    }

    public Customer merge(Customer customer) {
        log.debug("Merging customer {}", customer != null ? customer.getCustomerNumber() : "null");
        return em.merge(customer);
    }

    public Customer findById(Long id) {
        log.debug("Finding customer by id {}", id);
        return em.find(Customer.class, id);
    }

    public Customer findByCustomerNumber(String customerNumber) {
        log.debug("Finding customer by number {}", customerNumber);
        TypedQuery<Customer> query = em.createNamedQuery("Customer.findByCustomerNumber", Customer.class);
        query.setParameter("customerNumber", customerNumber);
        List<Customer> customers = query.getResultList();
        return customers.isEmpty() ? null : customers.get(0);
    }

    public List<Customer> findByName(String name) {
        log.debug("Finding customers by name {}", name);
        TypedQuery<Customer> query = em.createNamedQuery("Customer.findByName", Customer.class);
        query.setParameter("name", "%" + name + "%");
        return query.getResultList();
    }

    public Customer findByPersonId(Long personId) {
        log.debug("Finding customer by person id {}", personId);
        TypedQuery<Customer> query = em.createNamedQuery("Customer.findByPersonId", Customer.class);
        query.setParameter("personId", personId);
        List<Customer> customers = query.getResultList();
        return customers.isEmpty() ? null : customers.get(0);
    }

    public List<Customer> findAll() {
        log.debug("Finding all customers");
        TypedQuery<Customer> query = em.createNamedQuery("Customer.findAll", Customer.class);
        return query.getResultList();
    }

    public void remove(Customer customer) {
        log.debug("Removing customer {}", customer != null ? customer.getCustomerNumber() : "null");
        if (customer != null) {
            em.remove(em.contains(customer) ? customer : em.merge(customer));
        }
    }
}
