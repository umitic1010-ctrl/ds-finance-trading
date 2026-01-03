package net.froihofer.dsfinance.bank.web;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Permissive servlet CORS filter: allows any origin, mirrors requested headers and
 * short-circuits OPTIONS with 200.
 */
@WebFilter("/*")
public class AllowAllWebCorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String origin = req.getHeader("Origin");
        String allowOrigin = (origin == null || origin.isBlank()) ? "*" : origin;
        String requestHeaders = req.getHeader("Access-Control-Request-Headers");
        String allowHeaders = (requestHeaders != null && !requestHeaders.isBlank())
                ? requestHeaders
                : "Origin, Authorization, Content-Type, Accept";

        res.setHeader("Access-Control-Allow-Origin", allowOrigin);
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        res.setHeader("Access-Control-Allow-Headers", allowHeaders);
        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Max-Age", "3600");
        res.setHeader("Vary", "Origin");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }
}
