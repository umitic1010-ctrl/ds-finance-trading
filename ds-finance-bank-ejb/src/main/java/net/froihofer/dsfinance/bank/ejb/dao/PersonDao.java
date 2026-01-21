package net.froihofer.dsfinance.bank.ejb.dao;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import net.froihofer.dsfinance.bank.ejb.entity.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Stateless
@PermitAll
public class PersonDao {
    private static final Logger log = LoggerFactory.getLogger(PersonDao.class);

    @PersistenceContext(unitName = "ds-finance-bank-ref-persunit")
    private EntityManager em;

    public Person persist(Person person) {
        log.debug("Persisting person {}", person != null ? person.getEmail() : "null");
        em.persist(person);
        return person;
    }

    public Person findById(Long id) {
        log.debug("Finding person by id {}", id);
        return em.find(Person.class, id);
    }

    public Person findByEmail(String email) {
        log.debug("Finding person by email {}", email);
        TypedQuery<Person> query = em.createNamedQuery("Person.findByEmail", Person.class);
        query.setParameter("email", email);
        List<Person> people = query.getResultList();
        return people.isEmpty() ? null : people.get(0);
    }

    public Person merge(Person person) {
        log.debug("Merging person {}", person != null ? person.getEmail() : "null");
        return em.merge(person);
    }

    public void remove(Person person) {
        log.debug("Removing person {}", person != null ? person.getEmail() : "null");
        if (person != null) {
            em.remove(em.contains(person) ? person : em.merge(person));
        }
    }
}
