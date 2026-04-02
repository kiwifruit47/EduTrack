package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.ChangePasswordRequest;
import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.entity.RoleEnum;
import com.edutrack.e_journal.entity.School;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.SchoolRepository;
import com.edutrack.e_journal.repository.StudentRepository;
import com.edutrack.e_journal.repository.TeacherRepository;
import com.edutrack.e_journal.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository    userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository  schoolRepository;
    private final PasswordEncoder   passwordEncoder;

    @GetMapping
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails principal,
                                            @Valid @RequestBody ChangePasswordRequest req) {
        User user = resolveUser(principal);

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PutMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPicture(@AuthenticationPrincipal UserDetails principal,
                                           @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
        }
        User user = resolveUser(principal);
        user.setProfilePicture(file.getBytes());
        user.setProfilePictureType(file.getContentType());
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/bio")
    public ResponseEntity<UserDto> updateBio(@AuthenticationPrincipal UserDetails principal,
                                             @RequestBody Map<String, String> body) {
        User user = resolveUser(principal);
        String bio = body.getOrDefault("bio", "").strip();
        if (bio.length() > 500)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bio must be 500 characters or fewer");
        user.setBio(bio.isEmpty() ? null : bio);
        userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    @DeleteMapping("/picture")
    public ResponseEntity<Void> deletePicture(@AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        user.setProfilePicture(null);
        user.setProfilePictureType(null);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
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
                role.name(), schoolId, schoolName, u.getBio());
    }
}
