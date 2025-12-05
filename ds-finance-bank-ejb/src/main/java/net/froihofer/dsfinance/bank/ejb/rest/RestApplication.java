package net.froihofer.dsfinance.bank.ejb.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application Configuration
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    // rely on automatic classpath scanning for all resources/providers
}
