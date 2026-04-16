package com.edutrack.e_journal.service;

import com.edutrack.e_journal.dto.SchoolClassDto;
import com.edutrack.e_journal.dto.UserSummaryDto;
import com.edutrack.e_journal.entity.SchoolClass;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.ClassRepository;
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
public class ClassService {

    private final ClassRepository   classRepository;
    private final StudentRepository studentRepository;
    private final UserRepository    userRepository;
    private final SchoolService     schoolService;

    public List<SchoolClassDto> getAll(UserDetails principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return classRepository.findAll().stream().map(this::toDto).toList();
        }
        User headmaster = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Long schoolId = schoolService.resolveHeadmasterSchool(headmaster).getId();
        return classRepository.findAllBySchool_Id(schoolId).stream().map(this::toDto).toList();
    }

    public List<SchoolClassDto> getBySchool(Long schoolId) {
        return classRepository.findAllBySchool_Id(schoolId).stream()
                .map(this::toDto).toList();
    }

    public SchoolClassDto getById(Long id) {
        return classRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }

    public List<UserSummaryDto> getStudents(Long classId) {
        return studentRepository.findAllBySchoolClass_Id(classId).stream()
                .map(s -> new UserSummaryDto(
                        s.getId(),
                        s.getUser().getFirstName() + " " + s.getUser().getLastName()))
                .toList();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private SchoolClassDto toDto(SchoolClass c) {
        return new SchoolClassDto(c.getId(), c.getName(), c.getSchoolYear(),
                c.getSchool().getId(), c.getSchool().getName());
    }
}
