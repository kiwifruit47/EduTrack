package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.TeacherDto;
import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.entity.RoleEnum;
import com.edutrack.e_journal.entity.School;
import com.edutrack.e_journal.entity.Subject;
import com.edutrack.e_journal.entity.Teacher;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.SchoolRepository;
import com.edutrack.e_journal.repository.ScheduleRepository;
import com.edutrack.e_journal.repository.SubjectRepository;
import com.edutrack.e_journal.repository.TeacherRepository;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Tag(name = "Teachers", description = "Headmaster operations — hire, fire, salary, and qualifications")
@SecurityRequirement(name = "bearerAuth")
public class TeacherController {

    private final TeacherRepository  teacherRepository;
    private final UserRepository     userRepository;
    private final SchoolRepository   schoolRepository;
    private final ScheduleRepository scheduleRepository;
    private final SubjectRepository  subjectRepository;

    // ── List teachers at a school ─────────────────────────────────────────────

    @Operation(summary = "List teachers at a school", description = "Returns full teacher profiles — salary, qualifications, and class count. HEADMASTER (own school) or ADMIN.")
    @ApiResponse(responseCode = "200", description = "Teacher list returned")
    @GetMapping("/school/{schoolId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public List<TeacherDto> getBySchool(
            @Parameter(description = "School ID") @PathVariable Long schoolId,
            @AuthenticationPrincipal UserDetails principal) {

        checkHeadmasterSchoolAccess(principal, schoolId);

        return teacherRepository.findAllBySchool_Id(schoolId).stream()
                .map(this::toDto)
                .toList();
    }

    // ── Available (unassigned) teachers ───────────────────────────────────────

    @Operation(summary = "List available teachers", description = "Returns TEACHER-role users not currently assigned to any school. HEADMASTER only.")
    @ApiResponse(responseCode = "200", description = "Available teacher list returned")
    @GetMapping("/available")
    @PreAuthorize("hasRole('HEADMASTER')")
    public List<UserDto> getAvailable() {
        return userRepository.findAvailableTeachers().stream()
                .map(u -> new UserDto(u.getId(), u.getFirstName(), u.getLastName(),
                        u.getEmail(), u.getRole().getName().name(), null, null, u.getBio()))
                .toList();
    }

    // ── Hire ──────────────────────────────────────────────────────────────────

    @Operation(summary = "Hire a teacher", description = "Assigns a TEACHER-role user to the headmaster's school. HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Teacher hired"),
        @ApiResponse(responseCode = "409", description = "Teacher already assigned to a school")
    })
    @PostMapping("/{userId}/hire")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<UserDto> hire(
            @Parameter(description = "User ID of the teacher to hire") @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails principal) {

        User headmaster = resolveUser(principal);
        School school = resolveHeadmasterSchool(headmaster);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole().getName() != RoleEnum.TEACHER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a teacher");
        }

        Teacher teacher = teacherRepository.findById(userId).orElse(null);
        if (teacher != null && teacher.getSchool() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teacher is already assigned to a school");
        }

        if (teacher == null) {
            teacher = Teacher.builder().id(userId).user(user).school(school).build();
        } else {
            teacher.setSchool(school);
        }

        teacherRepository.save(teacher);
        return ResponseEntity.ok(new UserDto(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole().getName().name(), school.getId(), school.getName(),
                user.getBio()));
    }

    // ── Fire ──────────────────────────────────────────────────────────────────

    @Operation(summary = "Fire a teacher", description = "Removes a teacher from the headmaster's school. HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Teacher fired"),
        @ApiResponse(responseCode = "403", description = "Teacher does not belong to this school"),
        @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @DeleteMapping("/{teacherId}/fire")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<Void> fire(
            @Parameter(description = "Teacher user ID") @PathVariable Long teacherId,
            @AuthenticationPrincipal UserDetails principal) {

        User headmaster = resolveUser(principal);
        School school = resolveHeadmasterSchool(headmaster);

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        if (teacher.getSchool() == null || !teacher.getSchool().getId().equals(school.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher does not belong to this school");
        }

        teacher.setSchool(null);
        teacherRepository.save(teacher);
        return ResponseEntity.noContent().build();
    }

    // ── Salary ────────────────────────────────────────────────────────────────

    @Operation(summary = "Update teacher salary", description = "Sets the monthly gross salary for a teacher at the headmaster's school. HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Salary updated, full teacher profile returned"),
        @ApiResponse(responseCode = "403", description = "Teacher does not belong to this school"),
        @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @PutMapping("/{teacherId}/salary")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<TeacherDto> updateSalary(
            @Parameter(description = "Teacher user ID") @PathVariable Long teacherId,
            @RequestBody SalaryRequest req,
            @AuthenticationPrincipal UserDetails principal) {

        Teacher teacher = resolveOwnTeacher(teacherId, principal);
        teacher.setSalary(req.getSalary());
        return ResponseEntity.ok(toDto(teacherRepository.save(teacher)));
    }

    // ── Qualifications ────────────────────────────────────────────────────────

    @Operation(summary = "Update teacher qualifications", description = "Replaces the teacher's subject qualifications with the provided list. HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Qualifications updated, full teacher profile returned"),
        @ApiResponse(responseCode = "403", description = "Teacher does not belong to this school"),
        @ApiResponse(responseCode = "404", description = "Teacher or subject not found")
    })
    @PutMapping("/{teacherId}/qualifications")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<TeacherDto> updateQualifications(
            @Parameter(description = "Teacher user ID") @PathVariable Long teacherId,
            @RequestBody QualificationsRequest req,
            @AuthenticationPrincipal UserDetails principal) {

        Teacher teacher = resolveOwnTeacher(teacherId, principal);

        Set<Subject> subjects = new HashSet<>(subjectRepository.findAllById(req.getSubjectIds()));
        teacher.setQualifications(subjects);
        return ResponseEntity.ok(toDto(teacherRepository.save(teacher)));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Teacher resolveOwnTeacher(Long teacherId, UserDetails principal) {
        User headmaster = resolveUser(principal);
        School school = resolveHeadmasterSchool(headmaster);

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        if (teacher.getSchool() == null || !teacher.getSchool().getId().equals(school.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher does not belong to this school");
        }
        return teacher;
    }

    private void checkHeadmasterSchoolAccess(UserDetails principal, Long schoolId) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            User headmaster = resolveUser(principal);
            School school = resolveHeadmasterSchool(headmaster);
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

    private TeacherDto toDto(Teacher t) {
        String name = t.getUser().getFirstName() + " " + t.getUser().getLastName();
        String email = t.getUser().getEmail();

        List<TeacherDto.SubjectItem> qualifications = t.getQualifications().stream()
                .map(s -> new TeacherDto.SubjectItem(s.getId(), s.getName()))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();

        int classCount = (int) scheduleRepository.findAllByTeacher_Id(t.getId()).stream()
                .map(s -> s.getSchoolClass().getId())
                .distinct()
                .count();

        return new TeacherDto(t.getId(), name, email, t.getSalary(), qualifications, classCount);
    }

    // ── Inline request DTOs ───────────────────────────────────────────────────

    @Getter @NoArgsConstructor
    public static class SalaryRequest {
        private BigDecimal salary; // null clears the salary
    }

    @Getter @NoArgsConstructor
    public static class QualificationsRequest {
        private List<Long> subjectIds;
    }
}
