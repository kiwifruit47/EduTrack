package com.edutrack.e_journal.service;

import com.edutrack.e_journal.dto.*;
import com.edutrack.e_journal.entity.*;
import com.edutrack.e_journal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository    userRepository;
    private final RoleRepository    roleRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository  schoolRepository;
    private final PasswordEncoder   passwordEncoder;

    public List<UserDto> getAllUsers() {
        return ((List<User>) userRepository.findAll()).stream()
                .map(this::toUserDto)
                .toList();
    }

    public List<UserSummaryDto> getTeachersBySchool(Long schoolId) {
        return teacherRepository.findAllBySchool_Id(schoolId).stream()
                .map(t -> new UserSummaryDto(t.getId(),
                        t.getUser().getFirstName() + " " + t.getUser().getLastName()))
                .toList();
    }

    public List<UserDto> getStudentsBySchool(Long schoolId) {
        return studentRepository.findAllBySchool_Id(schoolId).stream()
                .map(s -> toUserDto(s.getUser()))
                .toList();
    }

    public List<UserDto> getParentsBySchool(Long schoolId) {
        return studentRepository.findAllBySchool_IdAndParentIsNotNull(schoolId).stream()
                .map(s -> s.getParent())
                .distinct()
                .map(this::toUserDto)
                .toList();
    }

    public List<UserSummaryDto> getHeadmasters() {
        return userRepository.findAllByRole_Name(RoleEnum.HEADMASTER).stream()
                .map(u -> new UserSummaryDto(u.getId(), u.getFirstName() + " " + u.getLastName()))
                .toList();
    }

    public ResponseEntity<byte[]> getPicture(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getProfilePicture() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(user.getProfilePictureType()))
                .body(user.getProfilePicture());
    }

    public UserDto createUser(CreateUserRequest req) {
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
        return toUserDto(userRepository.save(user));
    }

    public UserDto updateUser(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail());
        user.setRole(resolveRole(req.getRole()));
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        return toUserDto(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    // ── Shared helpers ─────────────────────────────────────────────────────────

    public User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public UserDto toUserDto(User u) {
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
                role.name(), schoolId, schoolName, u.getBio());
    }

    private Role resolveRole(String roleName) {
        RoleEnum roleEnum;
        try {
            roleEnum = RoleEnum.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName);
        }
        return roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Role not found: " + roleName));
    }
}
