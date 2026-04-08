package com.edutrack.e_journal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Grants ADMIN authority to any request that originates from localhost
 * (127.0.0.1 or ::1), as long as no authentication has already been
 * established by a prior filter (e.g. a valid JWT still takes precedence).
 *
 * NOT annotated with @Component — registered explicitly inside the Spring
 * Security filter chain via SecurityConfig.addFilterBefore(), so it runs
 * after SecurityContextHolderFilter initialises the context.
 */
public class LocalhostAdminFilter extends OncePerRequestFilter {

    private static final Set<String> LOOPBACK = Set.of("127.0.0.1", "0:0:0:0:0:0:0:1", "::1");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null
                && isLocalhost(request)) {

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "localhost-admin",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    private boolean isLocalhost(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return LOOPBACK.contains(ip);
    }
}
