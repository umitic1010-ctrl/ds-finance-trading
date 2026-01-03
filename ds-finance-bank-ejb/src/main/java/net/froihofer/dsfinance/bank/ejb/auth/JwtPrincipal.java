package net.froihofer.dsfinance.bank.ejb.auth;

import java.security.Principal;

public class JwtPrincipal implements Principal {
    private final Long personId;
    private final String role;
    private final Long customerId;

    public JwtPrincipal(Long personId, String role, Long customerId) {
        this.personId = personId;
        this.role = role;
        this.customerId = customerId;
    }

    @Override
    public String getName() {
        return personId != null ? personId.toString() : "";
    }

    public Long getPersonId() {
        return personId;
    }

    public String getRole() {
        return role;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
