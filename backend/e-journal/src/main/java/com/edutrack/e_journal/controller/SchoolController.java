package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.SchoolDto;
import com.edutrack.e_journal.dto.SchoolRequest;
import com.edutrack.e_journal.entity.*;
import com.edutrack.e_journal.repository.SchoolProfileRepository;
import com.edutrack.e_journal.repository.SchoolRepository;
import com.edutrack.e_journal.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolRepository        schoolRepository;
    private final UserRepository          userRepository;
    private final SchoolProfileRepository profileRepository;

    // ── Schools ──────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SchoolDto> getAll() {
        return schoolRepository.findAll().stream().map(this::toDto).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolDto> create(@Valid @RequestBody SchoolRequest req) {
        School school = School.builder()
                .name(req.getName())
                .address(req.getAddress())
                .type(resolveType(req.getType()))
                .director(resolveHeadmaster(req.getHeadmasterId()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(schoolRepository.save(school)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolDto> update(@PathVariable Long id, @Valid @RequestBody SchoolRequest req) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
        school.setName(req.getName());
        school.setAddress(req.getAddress());
        school.setType(resolveType(req.getType()));
        school.setDirector(resolveHeadmaster(req.getHeadmasterId()));
        return ResponseEntity.ok(toDto(schoolRepository.save(school)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!schoolRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found");
        }
        schoolRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Profiles ─────────────────────────────────────────────────────────────

    @GetMapping("/{schoolId}/profiles")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<SchoolDto.ProfileDto> getProfiles(@PathVariable Long schoolId) {
        return profileRepository.findAllBySchool_Id(schoolId).stream()
                .map(p -> new SchoolDto.ProfileDto(p.getId(), p.getName()))
                .toList();
    }

    @PostMapping("/{schoolId}/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolDto.ProfileDto> addProfile(
            @PathVariable Long schoolId,
            @Valid @RequestBody ProfileRequest req) {

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));

        SchoolProfile profile = SchoolProfile.builder()
                .name(req.getName())
                .school(school)
                .build();
        SchoolProfile saved = profileRepository.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SchoolDto.ProfileDto(saved.getId(), saved.getName()));
    }

    @DeleteMapping("/profiles/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
        }
        profileRepository.deleteById(profileId);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SchoolType resolveType(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return SchoolType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid school type: " + type);
        }
    }

    private User resolveHeadmaster(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Headmaster user not found"));
    }

    private SchoolDto toDto(School s) {
        String headmasterName = s.getDirector() != null
                ? s.getDirector().getFirstName() + " " + s.getDirector().getLastName()
                : null;
        String type = s.getType() != null ? s.getType().name() : null;
        List<SchoolDto.ProfileDto> profiles = profileRepository.findAllBySchool_Id(s.getId()).stream()
                .map(p -> new SchoolDto.ProfileDto(p.getId(), p.getName()))
                .toList();
        return new SchoolDto(s.getId(), s.getName(), s.getAddress(), type, headmasterName, profiles);
    }

    // ── Inline request DTO ───────────────────────────────────────────────────

    @Getter
    @NoArgsConstructor
    public static class ProfileRequest {
        @NotBlank @Size(max = 100)
        private String name;
    }
}
