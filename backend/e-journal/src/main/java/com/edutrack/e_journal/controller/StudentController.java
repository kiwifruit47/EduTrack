package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Headmaster operations — enroll and expel students from the school")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "List available students", description = "Returns STUDENT-role users not currently enrolled in any school. HEADMASTER only.")
    @ApiResponse(responseCode = "200", description = "Available student list returned")
    @GetMapping("/available")
    @PreAuthorize("hasRole('HEADMASTER')")
    public List<UserDto> getAvailable() {
        return studentService.getAvailable();
    }

    @Operation(summary = "Enroll a student", description = "Assigns the student to the headmaster's school. Creates a student record if one does not exist yet. HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student enrolled, updated profile returned"),
        @ApiResponse(responseCode = "400", description = "User is not a student or headmaster has no school"),
        @ApiResponse(responseCode = "409", description = "Student is already enrolled in a school")
    })
    @PostMapping("/{userId}/enroll")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<UserDto> enroll(
            @Parameter(description = "User ID of the student to enroll") @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(studentService.enroll(userId, principal));
    }

    @Operation(summary = "Expel a student", description = "Removes the student from the headmaster's school (sets school to null). HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Student expelled"),
        @ApiResponse(responseCode = "403", description = "Student does not belong to this headmaster's school"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @DeleteMapping("/{studentId}/expel")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<Void> expel(
            @Parameter(description = "Student user ID") @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails principal) {
        studentService.expel(studentId, principal);
        return ResponseEntity.noContent().build();
    }
}
