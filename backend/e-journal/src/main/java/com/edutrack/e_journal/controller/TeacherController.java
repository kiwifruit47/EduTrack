package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.TeacherDto;
import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Tag(name = "Teachers", description = "Headmaster operations — hire, fire, salary, and qualifications")
@SecurityRequirement(name = "bearerAuth")
public class TeacherController {

    private final TeacherService teacherService;

    @Operation(summary = "List teachers at a school", description = "Returns full teacher profiles — salary, qualifications, and class count. HEADMASTER (own school) or ADMIN.")
    @ApiResponse(responseCode = "200", description = "Teacher list returned")
    @GetMapping("/school/{schoolId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public List<TeacherDto> getBySchool(
            @Parameter(description = "School ID") @PathVariable Long schoolId,
            @AuthenticationPrincipal UserDetails principal) {
        return teacherService.getBySchool(schoolId, principal);
    }

    @Operation(summary = "List available teachers", description = "Returns TEACHER-role users not currently assigned to any school. HEADMASTER only.")
    @ApiResponse(responseCode = "200", description = "Available teacher list returned")
    @GetMapping("/available")
    @PreAuthorize("hasRole('HEADMASTER')")
    public List<UserDto> getAvailable() {
        return teacherService.getAvailable();
    }

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
        return ResponseEntity.ok(teacherService.hire(userId, principal));
    }

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
        teacherService.fire(teacherId, principal);
        return ResponseEntity.noContent().build();
    }

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
        return ResponseEntity.ok(teacherService.updateSalary(teacherId, req.getSalary(), principal));
    }

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
        return ResponseEntity.ok(teacherService.updateQualifications(teacherId, req.getSubjectIds(), principal));
    }

    // ── Inline request DTOs ───────────────────────────────────────────────────

    @Getter @NoArgsConstructor
    public static class SalaryRequest {
        private BigDecimal salary;
    }

    @Getter @NoArgsConstructor
    public static class QualificationsRequest {
        private List<Long> subjectIds;
    }
}
