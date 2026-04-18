package com.edutrack.e_journal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        // Capture the start time to calculate request latency
        long start = System.currentTimeMillis();
        try {
            // Delegate the request to the next filter in the Spring Security chain
            chain.doFilter(req, res);
        } finally {
            // Calculate total execution time
            long ms = System.currentTimeMillis() - start;
            // Log the HTTP method, URI, response status, latency, and client IP for audit/monitoring
            log.info("{} {} → {} ({}ms) [{}]",
                    req.getMethod(),
                    req.getRequestURI(),
                    res.getStatus(),
                    ms,
                    getClientIp(req));
        }
    }

    private String getClientIp(HttpServletRequest req) {
        // Extract the client's IP address, accounting for proxy/load balancer headers
        String forwarded = req.getHeader("X-Forwarded-For");
        // Use the first IP in the X-Forwarded-For chain if present, otherwise fallback to the direct remote address
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : req.getRemoteAddr();
    }
}
