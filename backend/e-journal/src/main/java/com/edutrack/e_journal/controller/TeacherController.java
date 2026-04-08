package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.entity.RoleEnum;
import com.edutrack.e_journal.entity.School;
import com.edutrack.e_journal.entity.Teacher;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.SchoolRepository;
import com.edutrack.e_journal.repository.TeacherRepository;
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
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Tag(name = "Teachers", description = "Headmaster operations — hire and fire teachers at the school")
@SecurityRequirement(name = "bearerAuth")
public class TeacherController {

    private final TeacherRepository teacherRepository;
    private final UserRepository    userRepository;
    private final SchoolRepository  schoolRepository;

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

    @Operation(summary = "Hire a teacher", description = "Assigns the teacher to the headmaster's school. Creates a teacher record if one does not exist yet. HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Teacher hired, updated profile returned"),
        @ApiResponse(responseCode = "400", description = "User is not a teacher or headmaster has no school"),
        @ApiResponse(responseCode = "409", description = "Teacher is already assigned to a school")
    })
    @PostMapping("/{userId}/hire")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<UserDto> hire(
            @Parameter(description = "User ID of the teacher to hire") @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails principal) {

        User headmaster = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        School school = schoolRepository.findByDirector_Id(headmaster.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No school assigned to this headmaster"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole().getName() != RoleEnum.TEACHER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a teacher");
        }

        Teacher teacher = teacherRepository.findById(userId).orElse(null);
        if (teacher != null && teacher.getSchool() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Teacher is already assigned to a school");
        }

        if (teacher == null) {
            teacher = Teacher.builder()
                    .id(userId)
                    .user(user)
                    .school(school)
                    .build();
        } else {
            teacher.setSchool(school);
        }

        teacherRepository.save(teacher);
        return ResponseEntity.ok(new UserDto(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole().getName().name(), school.getId(), school.getName(),
                user.getBio()));
    }

    @Operation(summary = "Fire a teacher", description = "Removes the teacher from the headmaster's school (sets school to null). HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Teacher fired"),
        @ApiResponse(responseCode = "403", description = "Teacher does not belong to this headmaster's school"),
        @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @DeleteMapping("/{teacherId}/fire")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<Void> fire(
            @Parameter(description = "Teacher user ID") @PathVariable Long teacherId,
            @AuthenticationPrincipal UserDetails principal) {

        User headmaster = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        School school = schoolRepository.findByDirector_Id(headmaster.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No school assigned to this headmaster"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        if (teacher.getSchool() == null || !teacher.getSchool().getId().equals(school.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Teacher does not belong to this school");
        }

        teacher.setSchool(null);
        teacherRepository.save(teacher);
        return ResponseEntity.noContent().build();
    }
}
