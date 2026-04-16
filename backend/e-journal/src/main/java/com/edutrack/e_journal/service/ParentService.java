package com.edutrack.e_journal.service;

import com.edutrack.e_journal.dto.ParentDto;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final UserRepository    userRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository  schoolRepository;
    private final SchoolService     schoolService;

    public List<ParentDto> getBySchool(Long schoolId, UserDetails principal) {
        schoolService.checkHeadmasterSchoolAccess(principal, schoolId);

        List<Student> studentsWithParent = studentRepository.findAllBySchool_IdAndParentIsNotNull(schoolId);

        Map<Long, User>                        parentById   = new LinkedHashMap<>();
        Map<Long, List<ParentDto.StudentItem>> childrenById = new LinkedHashMap<>();

        for (Student s : studentsWithParent) {
            User parent = s.getParent();
            parentById.put(parent.getId(), parent);
            childrenById
                .computeIfAbsent(parent.getId(), k -> new ArrayList<>())
                .add(new ParentDto.StudentItem(
                        s.getId(),
                        s.getUser().getFirstName() + " " + s.getUser().getLastName()));
        }

        return parentById.entrySet().stream()
                .map(e -> {
                    User p = e.getValue();
                    List<ParentDto.StudentItem> children = childrenById.get(e.getKey());
                    children.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    return new ParentDto(p.getId(), p.getFirstName(), p.getLastName(), p.getEmail(), children);
                })
                .toList();
    }

    public List<UserDto> getAvailable() {
        return userRepository.findAllByRole_Name(RoleEnum.PARENT).stream()
                .map(u -> new UserDto(u.getId(), u.getFirstName(), u.getLastName(),
                        u.getEmail(), RoleEnum.PARENT.name(), null, null, u.getBio()))
                .toList();
    }

    public ParentDto link(Long parentId, Long studentId, UserDetails principal) {
        User parent     = resolveParent(parentId);
        Student student = resolveOwnStudent(studentId, principal);
        student.setParent(parent);
        studentRepository.save(student);
        return toDto(parent, student.getSchool().getId());
    }

    public void unlink(Long parentId, Long studentId, UserDetails principal) {
        resolveParent(parentId);
        Student student = resolveOwnStudent(studentId, principal);
        student.setParent(null);
        studentRepository.save(student);
    }

    public ParentDto update(Long parentId, String firstName, String lastName, String email,
                            UserDetails principal) {
        User parent = resolveParent(parentId);
        School school = schoolService.resolveHeadmasterSchool(resolveUser(principal));
        parent.setFirstName(firstName);
        parent.setLastName(lastName);
        parent.setEmail(email);
        userRepository.save(parent);
        return toDto(parent, school.getId());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private User resolveParent(Long parentId) {
        User user = userRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole().getName() != RoleEnum.PARENT)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a parent");
        return user;
    }

    private Student resolveOwnStudent(Long studentId, UserDetails principal) {
        User headmaster = resolveUser(principal);
        School school   = schoolService.resolveHeadmasterSchool(headmaster);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (student.getSchool() == null || !student.getSchool().getId().equals(school.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student does not belong to this school");
        return student;
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private ParentDto toDto(User parent, Long schoolId) {
        List<ParentDto.StudentItem> children = studentRepository.findAllByParent_Id(parent.getId()).stream()
                .filter(s -> s.getSchool() != null && s.getSchool().getId().equals(schoolId))
                .map(s -> new ParentDto.StudentItem(
                        s.getId(),
                        s.getUser().getFirstName() + " " + s.getUser().getLastName()))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
        return new ParentDto(parent.getId(), parent.getFirstName(), parent.getLastName(),
                parent.getEmail(), children);
    }
}
