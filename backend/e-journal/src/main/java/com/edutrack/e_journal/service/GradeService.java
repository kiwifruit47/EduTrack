package com.edutrack.e_journal.service;

import com.edutrack.e_journal.dto.GradeDto;
import com.edutrack.e_journal.dto.GradeRequest;
import com.edutrack.e_journal.entity.Grade;
import com.edutrack.e_journal.entity.Schedule;
import com.edutrack.e_journal.entity.Student;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.GradeRepository;
import com.edutrack.e_journal.repository.ScheduleRepository;
import com.edutrack.e_journal.repository.StudentRepository;
import com.edutrack.e_journal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GradeService {

    private static final Set<BigDecimal> VALID_VALUES = Set.of(
        new BigDecimal("2.0"), new BigDecimal("2.5"),
        new BigDecimal("3.0"), new BigDecimal("3.5"),
        new BigDecimal("4.0"), new BigDecimal("4.5"),
        new BigDecimal("5.0"), new BigDecimal("5.5"),
        new BigDecimal("6.0")
    );

    private final GradeRepository    gradeRepository;
    private final StudentRepository  studentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository     userRepository;

    public List<GradeDto> getByClass(Long classId) {
        return gradeRepository.findAllBySchedule_SchoolClass_Id(classId).stream()
                .map(this::toDto).toList();
    }

    public List<GradeDto> getByCurrentStudent(UserDetails principal) {
        User user = resolveUser(principal);
        return gradeRepository.findAllByStudent_Id(user.getId()).stream()
                .map(this::toDto).toList();
    }

    public List<GradeDto> getByStudent(Long studentId) {
        return gradeRepository.findAllByStudent_Id(studentId).stream()
                .map(this::toDto).toList();
    }

    public GradeDto create(GradeRequest req) {
        if (!VALID_VALUES.contains(req.getValue().setScale(1)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Grade must be one of: 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6");
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found"));
        Schedule schedule = scheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule not found"));
        Grade grade = Grade.builder()
                .student(student)
                .schedule(schedule)
                .value(req.getValue())
                .build();
        return toDto(gradeRepository.save(grade));
    }

    public void delete(Long id) {
        if (!gradeRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found");
        gradeRepository.deleteById(id);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private GradeDto toDto(Grade g) {
        String studentName = g.getStudent().getUser().getFirstName()
                + " " + g.getStudent().getUser().getLastName();
        String teacherName = g.getSchedule().getTeacher().getUser().getFirstName()
                + " " + g.getSchedule().getTeacher().getUser().getLastName();
        return new GradeDto(
                g.getId(),
                g.getStudent().getId(),
                studentName,
                g.getSchedule().getId(),
                g.getSchedule().getSubject().getId(),
                g.getSchedule().getSubject().getName(),
                teacherName,
                g.getSchedule().getTerm(),
                g.getValue().toPlainString(),
                g.getCreatedAt() != null ? g.getCreatedAt().toString() : null
        );
    }
}
