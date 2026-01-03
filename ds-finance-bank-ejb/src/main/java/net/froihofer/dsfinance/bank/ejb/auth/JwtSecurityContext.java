package net.froihofer.dsfinance.bank.ejb.auth;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class JwtSecurityContext implements SecurityContext {
    private final JwtPrincipal principal;
    private final boolean secure;

    public JwtSecurityContext(JwtPrincipal principal, boolean secure) {
        this.principal = principal;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (principal == null || role == null) {
            return false;
        }
        String requested = role.trim();
        String actual = principal.getRole() == null ? "" : principal.getRole().trim();
        return actual.equalsIgnoreCase(requested);
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "JWT";
    }
}
