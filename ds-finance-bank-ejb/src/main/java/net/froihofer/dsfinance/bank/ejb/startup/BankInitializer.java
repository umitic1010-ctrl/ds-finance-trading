package net.froihofer.dsfinance.bank.ejb.startup;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.annotation.Resource;
import net.froihofer.dsfinance.bank.ejb.service.BankVolumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import org.flywaydb.core.Flyway;

/**
 * Startup Bean - Wird automatisch beim Deployment ausgeführt
 * Initialisiert die Bank mit dem Startkapital
 */
@Singleton
@Startup
public class BankInitializer {
    private static final Logger log = LoggerFactory.getLogger(BankInitializer.class);

    @EJB
    private BankVolumeService bankVolumeService;

    @Resource(lookup = "java:jboss/datasources/DsFinanceBankDS")
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        log.info("=".repeat(80));
        log.info("DS Finance Bank - Starting Initialization");
        log.info("=".repeat(80));

        try {
            // Flyway migration is disabled because the schema gets provisioned manually.
            // migrateDatabase();
            // Bank Volume initialisieren
            bankVolumeService.initializeBankVolume();
            log.info("✓ Bank Volume initialized: 1,000,000,000 USD");

            log.info("=".repeat(80));
            log.info("DS Finance Bank - Initialization Complete");
            log.info("REST API available at: /api/*");
            log.info("Test Client available at: /api-test.html");
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("ERROR during initialization", e);
            log.error("Please check your configuration and try again");
        }
    }

    // Keeping Flyway helper for future use when manual migration is no longer required.
    private void migrateDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
            log.info("✓ Database schema migrated");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to migrate database", e);
        }
    }
}
