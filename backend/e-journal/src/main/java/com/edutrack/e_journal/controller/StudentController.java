package com.edutrack.e_journal.controller;

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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Headmaster operations — enroll and expel students from the school")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentRepository studentRepository;
    private final UserRepository    userRepository;
    private final SchoolRepository  schoolRepository;

    @Operation(summary = "List available students", description = "Returns STUDENT-role users not currently enrolled in any school. HEADMASTER only.")
    @ApiResponse(responseCode = "200", description = "Available student list returned")
    @GetMapping("/available")
    @PreAuthorize("hasRole('HEADMASTER')")
    public List<UserDto> getAvailable() {
        return userRepository.findAvailableStudents().stream()
                .map(u -> new UserDto(u.getId(), u.getFirstName(), u.getLastName(),
                        u.getEmail(), u.getRole().getName().name(), null, null, u.getBio()))
                .toList();
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

        User headmaster = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        School school = schoolRepository.findByDirector_Id(headmaster.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No school assigned to this headmaster"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole().getName() != RoleEnum.STUDENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a student");
        }

        Student student = studentRepository.findById(userId).orElse(null);
        if (student != null && student.getSchool() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Student is already enrolled in a school");
        }

        if (student == null) {
            student = Student.builder()
                    .id(userId)
                    .user(user)
                    .school(school)
                    .build();
        } else {
            student.setSchool(school);
        }

        studentRepository.save(student);
        return ResponseEntity.ok(new UserDto(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole().getName().name(), school.getId(), school.getName(),
                user.getBio()));
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

        User headmaster = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        School school = schoolRepository.findByDirector_Id(headmaster.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No school assigned to this headmaster"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        if (student.getSchool() == null || !student.getSchool().getId().equals(school.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Student does not belong to this school");
        }

        student.setSchool(null);
        studentRepository.save(student);
        return ResponseEntity.noContent().build();
    }
}
