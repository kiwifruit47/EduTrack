package com.edutrack.e_journal.config;

import com.edutrack.e_journal.security.JwtAuthenticationFilter;
import com.edutrack.e_journal.security.LocalhostAdminFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.security.allow-localhost:true}")
    private boolean allowLocalhost;

    /**
     * Exposes BCryptPasswordEncoder as a bean.
     * Spring Boot auto-configuration picks this up alongside our CustomUserDetailsService
     * to wire a DaoAuthenticationProvider — no manual wiring needed.
     */
    @Bean
    // Configure the password hashing mechanism for the application
    public PasswordEncoder passwordEncoder() {
        // Use BCrypt with a strong, one-way hashing algorithm for secure credential storage
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the auto-configured AuthenticationManager so AuthController can inject it.
     */
    @Bean
    // Expose the AuthenticationManager bean to the Spring context for use in the AuthController
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Retrieve the pre-configured AuthenticationManager from the Spring Security configuration
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure the main security filter chain for the application
        http
            // Disable CSRF protection as we are using stateless JWT authentication
            .csrf(AbstractHttpConfigurer::disable)
            // Apply CORS configuration using the defined corsConfigurationSource bean
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Set session management to stateless to ensure no HttpSession is created or used
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Define authorization rules for incoming HTTP requests
            .authorizeHttpRequests(auth -> auth
                // Allow all requests to authentication endpoints (login, refresh, etc.)
                .requestMatchers("/auth/**").permitAll()
                // Permit access to Swagger/OpenAPI documentation endpoints
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // Require authentication for all other API requests
                .anyRequest().authenticated()
            )
            // Inject a custom filter to bypass security for localhost during development
            .addFilterBefore(new LocalhostAdminFilter(allowLocalhost), UsernamePasswordAuthenticationFilter.class)
            // Inject the JWT filter to validate Bearer tokens before the standard authentication filter
            .addFilterBefore(jwtAuthenticationFilter,   UsernamePasswordAuthenticationFilter.class);

        // Build and return the configured HttpSecurity instance
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Define the CORS policy for the application
        CorsConfiguration config = new CorsConfiguration();
        // Allow requests only from the Vite development server origin
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        // Permit standard HTTP verbs and preflight OPTIONS requests
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Allow all request headers to be processed
        config.setAllowedHeaders(List.of("*"));
        // Enable the inclusion of cookies and authentication headers in cross-origin requests
        config.setAllowCredentials(true);

        // Apply the defined configuration to all API endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
