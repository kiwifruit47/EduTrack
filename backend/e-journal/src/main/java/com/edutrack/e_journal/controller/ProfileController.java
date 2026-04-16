package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.ChangePasswordRequest;
import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Authenticated user's own profile — view, password, bio, and avatar")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Get my profile", description = "Returns the full profile of the currently authenticated user, including school affiliation.")
    @ApiResponse(responseCode = "200", description = "Profile returned")
    @GetMapping
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(profileService.getProfile(principal));
    }

    @Operation(summary = "Change password", description = "Validates the current password and sets a new one.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password changed"),
        @ApiResponse(responseCode = "400", description = "Current password incorrect or validation failed")
    })
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails principal,
                                            @Valid @RequestBody ChangePasswordRequest req) {
        profileService.changePassword(principal, req);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @Operation(summary = "Upload profile picture", description = "Accepts a multipart image (max ~2 MB recommended). Replaces any existing picture.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Picture saved"),
        @ApiResponse(responseCode = "400", description = "File missing or not an image")
    })
    @PutMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPicture(@AuthenticationPrincipal UserDetails principal,
                                           @RequestParam("file") MultipartFile file) throws IOException {
        profileService.uploadPicture(principal, file);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update bio", description = "Sets or clears the user's bio text (max 500 characters). Send `{ \"bio\": \"\" }` to clear.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bio updated, updated profile returned"),
        @ApiResponse(responseCode = "400", description = "Bio exceeds 500 characters")
    })
    @PutMapping("/bio")
    public ResponseEntity<UserDto> updateBio(@AuthenticationPrincipal UserDetails principal,
                                             @RequestBody Map<String, String> body) {
        String bio = body.getOrDefault("bio", "");
        return ResponseEntity.ok(profileService.updateBio(principal, bio));
    }

    @Operation(summary = "Delete profile picture", description = "Removes the user's profile picture.")
    @ApiResponse(responseCode = "204", description = "Picture deleted")
    @DeleteMapping("/picture")
    public ResponseEntity<Void> deletePicture(@AuthenticationPrincipal UserDetails principal) {
        profileService.deletePicture(principal);
        return ResponseEntity.noContent().build();
    }
}
