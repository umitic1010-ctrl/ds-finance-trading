package net.froihofer.dsfinance.bank.ejb.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import net.froihofer.dsfinance.bank.ejb.entity.DepotPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Stateless
public class DepotPositionDao {
    private static final Logger log = LoggerFactory.getLogger(DepotPositionDao.class);

    @PersistenceContext(unitName = "ds-finance-bank-ref-persunit")
    private EntityManager em;

    public DepotPosition persist(DepotPosition position) {
        log.debug("Persisting depot position for stock {}", position != null ? position.getStockSymbol() : "null");
        em.persist(position);
        return position;
    }

    public DepotPosition merge(DepotPosition position) {
        log.debug("Merging depot position for stock {}", position != null ? position.getStockSymbol() : "null");
        return em.merge(position);
    }

    public void remove(DepotPosition position) {
        log.debug("Removing depot position for stock {}", position != null ? position.getStockSymbol() : "null");
        em.remove(position);
    }

    public List<DepotPosition> findByCustomerId(Long customerId) {
        log.debug("Finding depot positions for customer id {}", customerId);
        TypedQuery<DepotPosition> query = em.createNamedQuery("DepotPosition.findByCustomer", DepotPosition.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    public DepotPosition findByCustomerAndStock(Long customerId, String stockSymbol) {
        log.debug("Finding depot position for customer id {} and stock {}", customerId, stockSymbol);
        TypedQuery<DepotPosition> query = em.createNamedQuery("DepotPosition.findByCustomerAndStock", DepotPosition.class);
        query.setParameter("customerId", customerId);
        query.setParameter("stockSymbol", stockSymbol);
        List<DepotPosition> positions = query.getResultList();
        return positions.isEmpty() ? null : positions.get(0);
    }
}
