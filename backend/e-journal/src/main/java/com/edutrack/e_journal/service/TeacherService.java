package com.edutrack.e_journal.service;

import com.edutrack.e_journal.dto.TeacherDto;
import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.entity.*;
import com.edutrack.e_journal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository  teacherRepository;
    private final UserRepository     userRepository;
    private final SchoolRepository   schoolRepository;
    private final ScheduleRepository scheduleRepository;
    private final SubjectRepository  subjectRepository;
    private final SchoolService      schoolService;

    public List<TeacherDto> getBySchool(Long schoolId, UserDetails principal) {
        schoolService.checkHeadmasterSchoolAccess(principal, schoolId);
        return teacherRepository.findAllBySchool_Id(schoolId).stream()
                .map(this::toDto).toList();
    }

    public List<UserDto> getAvailable() {
        return userRepository.findAvailableTeachers().stream()
                .map(u -> new UserDto(u.getId(), u.getFirstName(), u.getLastName(),
                        u.getEmail(), u.getRole().getName().name(), null, null, u.getBio()))
                .toList();
    }

    public UserDto hire(Long userId, UserDetails principal) {
        User headmaster = resolveUser(principal);
        School school = schoolService.resolveHeadmasterSchool(headmaster);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole().getName() != RoleEnum.TEACHER)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a teacher");

        Teacher teacher = teacherRepository.findById(userId).orElse(null);
        if (teacher != null && teacher.getSchool() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teacher is already assigned to a school");

        if (teacher == null) {
            teacher = Teacher.builder().id(userId).user(user).school(school).build();
        } else {
            teacher.setSchool(school);
        }

        teacherRepository.save(teacher);
        return new UserDto(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole().getName().name(),
                school.getId(), school.getName(), user.getBio());
    }

    public void fire(Long teacherId, UserDetails principal) {
        User headmaster = resolveUser(principal);
        School school = schoolService.resolveHeadmasterSchool(headmaster);

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        if (teacher.getSchool() == null || !teacher.getSchool().getId().equals(school.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher does not belong to this school");

        teacher.setSchool(null);
        teacherRepository.save(teacher);
    }

    public TeacherDto updateSalary(Long teacherId, BigDecimal salary, UserDetails principal) {
        Teacher teacher = resolveOwnTeacher(teacherId, principal);
        teacher.setSalary(salary);
        return toDto(teacherRepository.save(teacher));
    }

    public TeacherDto updateQualifications(Long teacherId, List<Long> subjectIds, UserDetails principal) {
        Teacher teacher = resolveOwnTeacher(teacherId, principal);
        Set<Subject> subjects = new HashSet<>(subjectRepository.findAllById(subjectIds));
        teacher.setQualifications(subjects);
        return toDto(teacherRepository.save(teacher));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Teacher resolveOwnTeacher(Long teacherId, UserDetails principal) {
        User headmaster = resolveUser(principal);
        School school = schoolService.resolveHeadmasterSchool(headmaster);
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));
        if (teacher.getSchool() == null || !teacher.getSchool().getId().equals(school.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher does not belong to this school");
        return teacher;
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private TeacherDto toDto(Teacher t) {
        String name  = t.getUser().getFirstName() + " " + t.getUser().getLastName();
        String email = t.getUser().getEmail();
        List<TeacherDto.SubjectItem> qualifications = t.getQualifications().stream()
                .map(s -> new TeacherDto.SubjectItem(s.getId(), s.getName()))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
        int classCount = (int) scheduleRepository.findAllByTeacher_Id(t.getId()).stream()
                .map(s -> s.getSchoolClass().getId())
                .distinct()
                .count();
        return new TeacherDto(t.getId(), name, email, t.getSalary(), qualifications, classCount);
    }
}
