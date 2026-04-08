package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.entity.RoleEnum;
import com.edutrack.e_journal.entity.School;
import com.edutrack.e_journal.entity.Teacher;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.SchoolRepository;
import com.edutrack.e_journal.repository.TeacherRepository;
import com.edutrack.e_journal.repository.UserRepository;
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
public class TeacherController {

    private final TeacherRepository teacherRepository;
    private final UserRepository    userRepository;
    private final SchoolRepository  schoolRepository;

    /** HEADMASTER: list TEACHER-role users not currently assigned to any school. */
    @GetMapping("/available")
    @PreAuthorize("hasRole('HEADMASTER')")
    public List<UserDto> getAvailable() {
        return userRepository.findAvailableTeachers().stream()
                .map(u -> new UserDto(u.getId(), u.getFirstName(), u.getLastName(),
                        u.getEmail(), u.getRole().getName().name(), null, null, u.getBio()))
                .toList();
    }

    /** HEADMASTER: hire a TEACHER-role user into the headmaster's school. */
    @PostMapping("/{userId}/hire")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<UserDto> hire(
            @PathVariable Long userId,
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

    /** HEADMASTER: fire (remove) a teacher from the headmaster's school. */
    @DeleteMapping("/{teacherId}/fire")
    @PreAuthorize("hasRole('HEADMASTER')")
    public ResponseEntity<Void> fire(
            @PathVariable Long teacherId,
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
