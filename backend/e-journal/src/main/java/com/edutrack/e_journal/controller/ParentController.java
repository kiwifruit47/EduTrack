package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.ParentDto;
import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.entity.RoleEnum;
import com.edutrack.e_journal.entity.School;
import com.edutrack.e_journal.entity.Student;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.SchoolRepository;
import com.edutrack.e_journal.repository.StudentRepository;
import com.edutrack.e_journal.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
@Tag(name = "Parents", description = "Headmaster operations — link/unlink parents to students and edit parent data")
@SecurityRequirement(name = "bearerAuth")
public class ParentController {

    private final UserRepository    userRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository  schoolRepository;

    // ── List parents at a school ──────────────────────────────────────────────

    @Operation(summary = "List parents at a school",
               description = "Returns parents with their children (filtered to the given school). One student has at most one parent. HEADMASTER (own school) or ADMIN.")
    @ApiResponse(responseCode = "200", description = "Parent list returned")
    @GetMapping("/school/{schoolId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public List<ParentDto> getBySchool(
            @Parameter(description = "School ID") @PathVariable Long schoolId,
            @AuthenticationPrincipal UserDetails principal) {
        checkHeadmasterSchoolAccess(principal, schoolId);

        List<Student> studentsWithParent = studentRepository.findAllBySchool_IdAndParentIsNotNull(schoolId);

        // Group students by parent, preserving first-seen order
        Map<Long, User>                    parentById   = new LinkedHashMap<>();
        Map<Long, List<ParentDto.StudentItem>> childrenById = new LinkedHashMap<>();

        for (Student s : studentsWithParent) {
            User parent = s.getParent();
            parentById.put(parent.getId(), parent);
            childrenById
                .computeIfAbsent(parent.getId(), k -> new ArrayList<>())
                .add(new ParentDto.StudentItem(
                        s.getId(),
                        s.getUser().getFirstName() + " " + s.getUser().getLastName()));
        }

        return parentById.entrySet().stream()
                .map(e -> {
                    User p = e.getValue();
                    List<ParentDto.StudentItem> children = childrenById.get(e.getKey());
                    children.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    return new ParentDto(p.getId(), p.getFirstName(), p.getLastName(), p.getEmail(), children);
                })
                .toList();
    }

    // ── All parent-role users (for link dialog) ────────────────────────────────

    @Operation(summary = "List all parent-role users",
               description = "Returns every user with the PARENT role for the link-parent dialog. HEADMASTER only.")
    @ApiResponse(responseCode = "200", description = "Parent user list returned")
    @GetMapping("/available")
    @PreAuthorize("hasRole('HEADMASTER')")
    public List<UserDto> getAvailable() {
        return userRepository.findAllByRole_Name(RoleEnum.PARENT).stream()
                .map(u -> new UserDto(u.getId(), u.getFirstName(), u.getLastName(),
                        u.getEmail(), RoleEnum.PARENT.name(), null, null, u.getBio()))
                .toList();
    }

    // ── Link parent → student ─────────────────────────────────────────────────

    @Operation(summary = "Link a parent to a student",
               description = "Sets the student's parent to the given user. Replaces any existing parent link. The student must belong to the headmaster's school. HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Linked — updated ParentDto returned"),
        @ApiResponse(responseCode = "400", description = "User is not a parent"),
        @ApiResponse(responseCode = "403", description = "Student does not belong to headmaster's school"),
        @ApiResponse(responseCode = "404", description = "Parent or student not found")
    })
    @PostMapping("/{parentId}/link/{studentId}")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<ParentDto> link(
            @Parameter(description = "Parent user ID") @PathVariable Long parentId,
            @Parameter(description = "Student user ID") @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails principal) {

        User parent     = resolveParent(parentId);
        Student student = resolveOwnStudent(studentId, principal);
        student.setParent(parent);
        studentRepository.save(student);
        return ResponseEntity.ok(toDto(parent, student.getSchool().getId()));
    }

    // ── Unlink parent → student ───────────────────────────────────────────────

    @Operation(summary = "Unlink a parent from a student",
               description = "Clears the student's parent field. The student must belong to the headmaster's school. HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Unlinked successfully"),
        @ApiResponse(responseCode = "403", description = "Student does not belong to headmaster's school"),
        @ApiResponse(responseCode = "404", description = "Parent or student not found")
    })
    @DeleteMapping("/{parentId}/unlink/{studentId}")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<Void> unlink(
            @Parameter(description = "Parent user ID") @PathVariable Long parentId,
            @Parameter(description = "Student user ID") @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails principal) {

        resolveParent(parentId); // validate it's a parent user
        Student student = resolveOwnStudent(studentId, principal);
        student.setParent(null);
        studentRepository.save(student);
        return ResponseEntity.noContent().build();
    }

    // ── Edit parent data ──────────────────────────────────────────────────────

    @Operation(summary = "Update parent data",
               description = "Updates a parent's first name, last name, and email. HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Parent updated — updated ParentDto returned"),
        @ApiResponse(responseCode = "404", description = "Parent not found")
    })
    @PutMapping("/{parentId}")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<ParentDto> update(
            @Parameter(description = "Parent user ID") @PathVariable Long parentId,
            @RequestBody UpdateParentRequest req,
            @AuthenticationPrincipal UserDetails principal) {

        User parent = resolveParent(parentId);
        School school = resolveHeadmasterSchool(resolveUser(principal));
        parent.setFirstName(req.getFirstName());
        parent.setLastName(req.getLastName());
        parent.setEmail(req.getEmail());
        userRepository.save(parent);
        return ResponseEntity.ok(toDto(parent, school.getId()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User resolveParent(Long parentId) {
        User user = userRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole().getName() != RoleEnum.PARENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a parent");
        }
        return user;
    }

    private Student resolveOwnStudent(Long studentId, UserDetails principal) {
        User headmaster = resolveUser(principal);
        School school   = resolveHeadmasterSchool(headmaster);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (student.getSchool() == null || !student.getSchool().getId().equals(school.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student does not belong to this school");
        }
        return student;
    }

    private void checkHeadmasterSchoolAccess(UserDetails principal, Long schoolId) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            User headmaster = resolveUser(principal);
            School school   = resolveHeadmasterSchool(headmaster);
            if (!school.getId().equals(schoolId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this school");
            }
        }
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private School resolveHeadmasterSchool(User headmaster) {
        return schoolRepository.findByDirector_Id(headmaster.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No school assigned to this headmaster"));
    }

    private ParentDto toDto(User parent, Long schoolId) {
        List<ParentDto.StudentItem> children = studentRepository.findAllByParent_Id(parent.getId()).stream()
                .filter(s -> s.getSchool() != null && s.getSchool().getId().equals(schoolId))
                .map(s -> new ParentDto.StudentItem(
                        s.getId(),
                        s.getUser().getFirstName() + " " + s.getUser().getLastName()))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
        return new ParentDto(parent.getId(), parent.getFirstName(), parent.getLastName(),
                parent.getEmail(), children);
    }

    // ── Inline request DTO ────────────────────────────────────────────────────

    @Getter @NoArgsConstructor
    public static class UpdateParentRequest {
        private String firstName;
        private String lastName;
        private String email;
    }
}
