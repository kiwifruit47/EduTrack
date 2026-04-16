package com.edutrack.e_journal.service;

import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.entity.RoleEnum;
import com.edutrack.e_journal.entity.School;
import com.edutrack.e_journal.entity.Student;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.SchoolRepository;
import com.edutrack.e_journal.repository.StudentRepository;
import com.edutrack.e_journal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository    userRepository;
    private final SchoolRepository  schoolRepository;

    public List<UserDto> getAvailable() {
        return userRepository.findAvailableStudents().stream()
                .map(u -> new UserDto(u.getId(), u.getFirstName(), u.getLastName(),
                        u.getEmail(), u.getRole().getName().name(), null, null, u.getBio()))
                .toList();
    }

    public UserDto enroll(Long userId, UserDetails principal) {
        User headmaster = resolveUser(principal);
        School school = resolveHeadmasterSchool(headmaster);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole().getName() != RoleEnum.STUDENT)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a student");

        Student student = studentRepository.findById(userId).orElse(null);
        if (student != null && student.getSchool() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student is already enrolled in a school");

        if (student == null) {
            student = Student.builder().id(userId).user(user).school(school).build();
        } else {
            student.setSchool(school);
        }

        studentRepository.save(student);
        return new UserDto(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole().getName().name(),
                school.getId(), school.getName(), user.getBio());
    }

    public void expel(Long studentId, UserDetails principal) {
        User headmaster = resolveUser(principal);
        School school = resolveHeadmasterSchool(headmaster);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        if (student.getSchool() == null || !student.getSchool().getId().equals(school.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student does not belong to this school");

        student.setSchool(null);
        studentRepository.save(student);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private School resolveHeadmasterSchool(User headmaster) {
        return schoolRepository.findByDirector_Id(headmaster.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No school assigned to this headmaster"));
    }
}
