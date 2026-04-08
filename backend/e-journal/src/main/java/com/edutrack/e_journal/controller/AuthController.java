package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.AuthResponse;
import com.edutrack.e_journal.dto.LoginRequest;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.UserRepository;
import com.edutrack.e_journal.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, token refresh, and logout")
public class AuthController {

    private static final String REFRESH_COOKIE = "refreshToken";
    private static final int   REFRESH_MAX_AGE  = 7 * 24 * 60 * 60; // 7 days in seconds

    private final AuthenticationManager authenticationManager;
    private final UserRepository        userRepository;
    private final JwtUtils              jwtUtils;

    @Operation(summary = "Log in", description = "Authenticate with email and password. Returns a short-lived access token in the body and sets a 7-day HttpOnly refresh cookie.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authenticated — access token returned"),
        @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletResponse response) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        String accessToken  = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        addRefreshCookie(response, refreshToken);

        return ResponseEntity.ok(new AuthResponse(accessToken));
    }

    @Operation(summary = "Refresh access token", description = "Uses the HttpOnly refresh cookie to issue a new access token. No request body needed.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New access token returned"),
        @ApiResponse(responseCode = "401", description = "Refresh token missing or expired")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshCookie(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token missing"));

        if (!jwtUtils.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        String email = jwtUtils.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return ResponseEntity.ok(new AuthResponse(jwtUtils.generateAccessToken(user)));
    }

    @Operation(summary = "Log out", description = "Clears the refresh cookie. The access token expires naturally (24 h).")
    @ApiResponse(responseCode = "200", description = "Logged out")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------

    private void addRefreshCookie(HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(REFRESH_COOKIE, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);   // set true in production (HTTPS)
        cookie.setPath("/auth");
        cookie.setMaxAge(REFRESH_MAX_AGE);
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Optional<String> extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
