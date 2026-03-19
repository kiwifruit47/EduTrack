package com.edutrack.e_journal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String ip        = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String timestamp = Instant.now().toString();

            if (!jwtUtils.validateToken(token)) {
                log.warn("[JWT] FAILED | ts={} | ip={} | ua={} | reason=invalid_or_expired_token",
                        timestamp, ip, userAgent);
            } else {
                String email;
                try {
                    email = jwtUtils.getEmailFromToken(token);
                } catch (Exception e) {
                    log.warn("[JWT] FAILED | ts={} | ip={} | ua={} | reason=cannot_extract_email | error={}",
                            timestamp, ip, userAgent, e.getMessage());
                    chain.doFilter(request, response);
                    return;
                }

                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.info("[JWT] OK | ts={} | ip={} | ua={} | user={} | roles={}",
                            timestamp, ip, userAgent, email, userDetails.getAuthorities());

                } catch (Exception e) {
                    log.warn("[JWT] FAILED | ts={} | ip={} | ua={} | user={} | reason=user_not_found | error={}",
                            timestamp, ip, userAgent, email, e.getMessage());
                }
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
