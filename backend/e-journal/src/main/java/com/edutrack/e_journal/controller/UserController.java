package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.*;
import com.edutrack.e_journal.entity.Role;
import com.edutrack.e_journal.entity.RoleEnum;
import com.edutrack.e_journal.entity.School;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.RoleRepository;
import com.edutrack.e_journal.repository.SchoolRepository;
import com.edutrack.e_journal.repository.StudentRepository;
import com.edutrack.e_journal.repository.TeacherRepository;
import com.edutrack.e_journal.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository    userRepository;
    private final RoleRepository    roleRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository  schoolRepository;
    private final PasswordEncoder   passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getAll() {
        return ((List<User>) userRepository.findAll()).stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/teachers/school/{schoolId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public List<UserSummaryDto> getTeachersBySchool(@PathVariable Long schoolId) {
        return teacherRepository.findAllBySchool_Id(schoolId).stream()
                .map(t -> new UserSummaryDto(t.getId(), t.getUser().getFirstName() + " " + t.getUser().getLastName()))
                .toList();
    }

    @GetMapping("/students/school/{schoolId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public List<UserDto> getStudentsBySchool(@PathVariable Long schoolId) {
        return studentRepository.findAllBySchool_Id(schoolId).stream()
                .map(s -> toDto(s.getUser()))
                .toList();
    }

    @GetMapping("/parents/school/{schoolId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public List<UserDto> getParentsBySchool(@PathVariable Long schoolId) {
        return userRepository.findAllByChildren_School_Id(schoolId).stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/headmasters")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserSummaryDto> getHeadmasters() {
        return userRepository.findAllByRole_Name(RoleEnum.HEADMASTER).stream()
                .map(u -> new UserSummaryDto(u.getId(), u.getFirstName() + " " + u.getLastName()))
                .toList();
    }

    @GetMapping("/{id}/picture")
    public ResponseEntity<byte[]> getPicture(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getProfilePicture() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(user.getProfilePictureType()))
                .body(user.getProfilePicture());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        Role role = resolveRole(req.getRole());
        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(userRepository.save(user)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail());
        user.setRole(resolveRole(req.getRole()));
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        return ResponseEntity.ok(toDto(userRepository.save(user)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------

    private Role resolveRole(String roleName) {
        RoleEnum roleEnum;
        try {
            roleEnum = RoleEnum.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName);
        }
        return roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found: " + roleName));
    }

    private UserDto toDto(User u) {
        Long schoolId = null;
        String schoolName = null;

        RoleEnum role = u.getRole().getName();
        if (role == RoleEnum.TEACHER) {
            var t = teacherRepository.findById(u.getId()).orElse(null);
            if (t != null && t.getSchool() != null) {
                schoolId   = t.getSchool().getId();
                schoolName = t.getSchool().getName();
            }
        } else if (role == RoleEnum.STUDENT) {
            var s = studentRepository.findById(u.getId()).orElse(null);
            if (s != null && s.getSchool() != null) {
                schoolId   = s.getSchool().getId();
                schoolName = s.getSchool().getName();
            }
        } else if (role == RoleEnum.HEADMASTER) {
            School school = schoolRepository.findByDirector_Id(u.getId()).orElse(null);
            if (school != null) {
                schoolId   = school.getId();
                schoolName = school.getName();
            }
        }

        return new UserDto(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getRole().getName().name(), schoolId, schoolName);
    }
}
