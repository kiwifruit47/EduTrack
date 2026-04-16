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
 * When {@code app.security.allow-localhost=true} (the default), any request
 * from 127.0.0.1 / ::1 that carries no JWT is automatically authenticated with
 * every role, bypassing all {@code @PreAuthorize} checks.
 *
 * Set {@code app.security.allow-localhost=false} to disable this behaviour
 * (e.g. in staging / production).
 *
 * NOT annotated with @Component — registered explicitly in SecurityConfig.
 */
public class LocalhostAdminFilter extends OncePerRequestFilter {

    private static final Set<String> LOOPBACK = Set.of("127.0.0.1", "0:0:0:0:0:0:0:1", "::1");

    private static final List<SimpleGrantedAuthority> ALL_ROLES = List.of(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_HEADMASTER"),
            new SimpleGrantedAuthority("ROLE_TEACHER"),
            new SimpleGrantedAuthority("ROLE_STUDENT"),
            new SimpleGrantedAuthority("ROLE_PARENT")
    );

    private final boolean enabled;

    public LocalhostAdminFilter(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (enabled
                && SecurityContextHolder.getContext().getAuthentication() == null
                && LOOPBACK.contains(request.getRemoteAddr())) {

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("localhost-admin", null, ALL_ROLES)
            );
        }

        chain.doFilter(request, response);
    }
}
