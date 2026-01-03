package net.froihofer.dsfinance.bank.ejb.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import net.froihofer.dsfinance.bank.ejb.entity.BankVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Stateless
public class BankVolumeDao {
    private static final Logger log = LoggerFactory.getLogger(BankVolumeDao.class);

    @PersistenceContext(unitName = "ds-finance-bank-ref-persunit")
    private EntityManager em;

    public BankVolume persist(BankVolume volume) {
        log.debug("Persisting bank volume");
        em.persist(volume);
        return volume;
    }

    public BankVolume merge(BankVolume volume) {
        log.debug("Merging bank volume");
        return em.merge(volume);
    }

    public BankVolume findCurrent() {
        log.debug("Finding current bank volume");
        TypedQuery<BankVolume> query = em.createNamedQuery("BankVolume.findCurrent", BankVolume.class);
        List<BankVolume> volumes = query.getResultList();
        return volumes.isEmpty() ? null : volumes.get(0);
    }
}
