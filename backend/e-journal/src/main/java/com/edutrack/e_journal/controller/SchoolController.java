package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.SchoolDto;
import com.edutrack.e_journal.dto.SchoolRequest;
import com.edutrack.e_journal.dto.SchoolScheduleEntryDto;
import com.edutrack.e_journal.dto.SchoolScheduleEntryRequest;
import com.edutrack.e_journal.dto.SchoolTermConfigDto;
import com.edutrack.e_journal.entity.*;
import com.edutrack.e_journal.repository.SchoolProfileRepository;
import com.edutrack.e_journal.repository.SchoolRepository;
import com.edutrack.e_journal.repository.SchoolScheduleEntryRepository;
import com.edutrack.e_journal.repository.SchoolTermConfigRepository;
import com.edutrack.e_journal.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
@Tag(name = "Schools", description = "School management — CRUD, profiles, daily schedule, term configuration, and student limit")
@SecurityRequirement(name = "bearerAuth")
public class SchoolController {

    private final SchoolRepository              schoolRepository;
    private final UserRepository                userRepository;
    private final SchoolProfileRepository       profileRepository;
    private final SchoolScheduleEntryRepository scheduleEntryRepository;
    private final SchoolTermConfigRepository    termConfigRepository;

    private static final SchoolTermConfigDto DEFAULT_TERM_CONFIG =
            new SchoolTermConfigDto("09-15", "02-01", "06-01", "06-15", "07-01");

    // ── Schools ──────────────────────────────────────────────────────────────

    @Operation(summary = "List all schools", description = "Returns every school with its headmaster and profiles. ADMIN only.")
    @ApiResponse(responseCode = "200", description = "School list returned")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SchoolDto> getAll() {
        return schoolRepository.findAll().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Get a school by ID", description = "Returns a single school. Accessible by ADMIN, HEADMASTER, and TEACHER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "School returned"),
        @ApiResponse(responseCode = "404", description = "School not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public SchoolDto getById(
            @Parameter(description = "School ID") @PathVariable Long id) {
        return schoolRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
    }

    @Operation(summary = "Create a school", description = "Creates a new school, optionally assigning a headmaster. ADMIN only.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "School created"),
        @ApiResponse(responseCode = "400", description = "Validation error or headmaster not found")
    })
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

    @Operation(summary = "Update a school", description = "Updates name, address, type, and headmaster. ADMIN only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "School updated"),
        @ApiResponse(responseCode = "404", description = "School not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolDto> update(
            @Parameter(description = "School ID") @PathVariable Long id,
            @Valid @RequestBody SchoolRequest req) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
        school.setName(req.getName());
        school.setAddress(req.getAddress());
        school.setType(resolveType(req.getType()));
        school.setDirector(resolveHeadmaster(req.getHeadmasterId()));
        return ResponseEntity.ok(toDto(schoolRepository.save(school)));
    }

    @Operation(summary = "Delete a school", description = "Permanently deletes a school. ADMIN only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "School deleted"),
        @ApiResponse(responseCode = "404", description = "School not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "School ID") @PathVariable Long id) {
        if (!schoolRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found");
        }
        schoolRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Profiles ─────────────────────────────────────────────────────────────

    @Operation(summary = "List school profiles", description = "Returns the specialisation profiles defined for a school (e.g. 'Natural Sciences'). Accessible by ADMIN, HEADMASTER, and TEACHER.")
    @ApiResponse(responseCode = "200", description = "Profile list returned")
    @GetMapping("/{schoolId}/profiles")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<SchoolDto.ProfileDto> getProfiles(
            @Parameter(description = "School ID") @PathVariable Long schoolId) {
        return profileRepository.findAllBySchool_Id(schoolId).stream()
                .map(p -> new SchoolDto.ProfileDto(p.getId(), p.getName()))
                .toList();
    }

    @Operation(summary = "Add a profile to a school", description = "Adds a new specialisation profile. ADMIN only.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Profile added"),
        @ApiResponse(responseCode = "404", description = "School not found")
    })
    @PostMapping("/{schoolId}/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolDto.ProfileDto> addProfile(
            @Parameter(description = "School ID") @PathVariable Long schoolId,
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

    @Operation(summary = "Delete a school profile", description = "Removes a specialisation profile. ADMIN only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Profile deleted"),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @DeleteMapping("/profiles/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfile(
            @Parameter(description = "Profile ID") @PathVariable Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
        }
        profileRepository.deleteById(profileId);
        return ResponseEntity.noContent().build();
    }

    // ── Daily Schedule ────────────────────────────────────────────────────────

    @Operation(summary = "Get school daily schedule", description = "Returns the ordered list of LECTURE/BREAK/SPECIAL_EVENT entries for the school day. Accessible by ADMIN, HEADMASTER, and TEACHER.")
    @ApiResponse(responseCode = "200", description = "Daily schedule returned")
    @GetMapping("/{schoolId}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<SchoolScheduleEntryDto> getSchedule(
            @Parameter(description = "School ID") @PathVariable Long schoolId) {
        return scheduleEntryRepository.findAllBySchool_IdOrderBySortOrder(schoolId).stream()
                .map(this::toScheduleDto)
                .toList();
    }

    @Operation(summary = "Add a daily schedule entry", description = "Appends a new entry to the school's daily schedule. Accessible by ADMIN and the school's own HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Entry added"),
        @ApiResponse(responseCode = "403", description = "Headmaster does not own this school"),
        @ApiResponse(responseCode = "404", description = "School not found")
    })
    @PostMapping("/{schoolId}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ResponseEntity<SchoolScheduleEntryDto> addScheduleEntry(
            @Parameter(description = "School ID") @PathVariable Long schoolId,
            @Valid @RequestBody SchoolScheduleEntryRequest req,
            @AuthenticationPrincipal UserDetails principal) {

        checkHeadmasterSchoolAccess(principal, schoolId);

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));

        int nextOrder = scheduleEntryRepository.findAllBySchool_IdOrderBySortOrder(schoolId).size();

        SchoolScheduleEntry entry = SchoolScheduleEntry.builder()
                .school(school)
                .type(resolveEntryType(req.getType()))
                .label(req.getLabel())
                .startTime(LocalTime.parse(req.getStartTime()))
                .endTime(LocalTime.parse(req.getEndTime()))
                .eventDate(parseDate(req.getEventDate()))
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : nextOrder)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toScheduleDto(scheduleEntryRepository.save(entry)));
    }

    @Operation(summary = "Update a daily schedule entry", description = "Edits an existing school day entry. Accessible by ADMIN and the school's own HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entry updated"),
        @ApiResponse(responseCode = "403", description = "Headmaster does not own this school"),
        @ApiResponse(responseCode = "404", description = "Entry not found")
    })
    @PutMapping("/schedule/{entryId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ResponseEntity<SchoolScheduleEntryDto> updateScheduleEntry(
            @Parameter(description = "Schedule entry ID") @PathVariable Long entryId,
            @Valid @RequestBody SchoolScheduleEntryRequest req,
            @AuthenticationPrincipal UserDetails principal) {

        SchoolScheduleEntry entry = scheduleEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule entry not found"));

        checkHeadmasterSchoolAccess(principal, entry.getSchool().getId());

        entry.setType(resolveEntryType(req.getType()));
        entry.setLabel(req.getLabel());
        entry.setStartTime(LocalTime.parse(req.getStartTime()));
        entry.setEndTime(LocalTime.parse(req.getEndTime()));
        entry.setEventDate(parseDate(req.getEventDate()));
        if (req.getSortOrder() != null) entry.setSortOrder(req.getSortOrder());

        return ResponseEntity.ok(toScheduleDto(scheduleEntryRepository.save(entry)));
    }

    @Operation(summary = "Delete a daily schedule entry", description = "Removes an entry from the school day schedule. Accessible by ADMIN and the school's own HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Entry deleted"),
        @ApiResponse(responseCode = "403", description = "Headmaster does not own this school"),
        @ApiResponse(responseCode = "404", description = "Entry not found")
    })
    @DeleteMapping("/schedule/{entryId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ResponseEntity<Void> deleteScheduleEntry(
            @Parameter(description = "Schedule entry ID") @PathVariable Long entryId,
            @AuthenticationPrincipal UserDetails principal) {

        SchoolScheduleEntry entry = scheduleEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule entry not found"));

        checkHeadmasterSchoolAccess(principal, entry.getSchool().getId());

        scheduleEntryRepository.deleteById(entryId);
        return ResponseEntity.noContent().build();
    }

    // ── Term Config ───────────────────────────────────────────────────────────

    @Operation(summary = "Get term configuration", description = "Returns the school-year term dates (MM-dd format). Falls back to system defaults if not yet configured. Accessible by ADMIN, HEADMASTER, and TEACHER.")
    @ApiResponse(responseCode = "200", description = "Term config returned")
    @GetMapping("/{schoolId}/term-config")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public SchoolTermConfigDto getTermConfig(
            @Parameter(description = "School ID") @PathVariable Long schoolId) {
        return termConfigRepository.findById(schoolId)
                .map(c -> new SchoolTermConfigDto(c.getStartDate(), c.getTerm2Start(),
                        c.getElementaryEnd(), c.getProgymnasiumEnd(), c.getGymnasiumEnd()))
                .orElse(DEFAULT_TERM_CONFIG);
    }

    @Operation(summary = "Update term configuration", description = "Saves custom term dates for a school. All dates use MM-dd format (e.g. '09-15'). Accessible by ADMIN and the school's own HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Term config saved"),
        @ApiResponse(responseCode = "403", description = "Headmaster does not own this school"),
        @ApiResponse(responseCode = "404", description = "School not found")
    })
    @PutMapping("/{schoolId}/term-config")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ResponseEntity<SchoolTermConfigDto> updateTermConfig(
            @Parameter(description = "School ID") @PathVariable Long schoolId,
            @RequestBody SchoolTermConfigDto req,
            @AuthenticationPrincipal UserDetails principal) {

        checkHeadmasterSchoolAccess(principal, schoolId);

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));

        SchoolTermConfig config = termConfigRepository.findById(schoolId).orElse(
                SchoolTermConfig.builder().id(schoolId).school(school)
                        .startDate("09-15").term2Start("02-01")
                        .elementaryEnd("06-01").progymnasiumEnd("06-15").gymnasiumEnd("07-01")
                        .build());

        config.setStartDate(req.getStartDate());
        config.setTerm2Start(req.getTerm2Start());
        config.setElementaryEnd(req.getElementaryEnd());
        config.setProgymnasiumEnd(req.getProgymnasiumEnd());
        config.setGymnasiumEnd(req.getGymnasiumEnd());

        termConfigRepository.save(config);
        return ResponseEntity.ok(req);
    }

    // ── Student Limit ─────────────────────────────────────────────────────────

    @Operation(summary = "Set student enrolment limit", description = "Sets the maximum number of students allowed in this school. Send `{ \"studentLimit\": null }` to remove the limit. Accessible by ADMIN and the school's own HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Limit saved"),
        @ApiResponse(responseCode = "403", description = "Headmaster does not own this school"),
        @ApiResponse(responseCode = "404", description = "School not found")
    })
    @PutMapping("/{schoolId}/student-limit")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ResponseEntity<Void> updateStudentLimit(
            @Parameter(description = "School ID") @PathVariable Long schoolId,
            @RequestBody StudentLimitRequest req,
            @AuthenticationPrincipal UserDetails principal) {

        checkHeadmasterSchoolAccess(principal, schoolId);

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));

        school.setStudentLimit(req.getStudentLimit());
        schoolRepository.save(school);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * If the caller is a HEADMASTER, verify they own the given school.
     * ADMIN users bypass this check.
     */
    private void checkHeadmasterSchoolAccess(UserDetails principal, Long schoolId) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            User headmaster = userRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            School headmasterSchool = schoolRepository.findByDirector_Id(headmaster.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "No school assigned to this headmaster"));
            if (!headmasterSchool.getId().equals(schoolId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied to this school's schedule");
            }
        }
    }

    private SchoolType resolveType(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return SchoolType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid school type: " + type);
        }
    }

    private ScheduleEntryType resolveEntryType(String type) {
        try {
            return ScheduleEntryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid entry type: " + type);
        }
    }

    private LocalDate parseDate(String date) {
        return (date != null && !date.isBlank()) ? LocalDate.parse(date) : null;
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
        return new SchoolDto(s.getId(), s.getName(), s.getAddress(), type, headmasterName, profiles, s.getStudentLimit());
    }

    private SchoolScheduleEntryDto toScheduleDto(SchoolScheduleEntry e) {
        return new SchoolScheduleEntryDto(
                e.getId(),
                e.getType().name(),
                e.getLabel(),
                e.getStartTime().toString(),
                e.getEndTime().toString(),
                e.getEventDate() != null ? e.getEventDate().toString() : null,
                e.getSortOrder()
        );
    }

    // ── Inline request DTOs ───────────────────────────────────────────────────

    @Getter
    @NoArgsConstructor
    public static class ProfileRequest {
        @NotBlank @Size(max = 100)
        private String name;
    }

    @Getter
    @NoArgsConstructor
    public static class StudentLimitRequest {
        private Integer studentLimit; // null removes the limit
    }
}
