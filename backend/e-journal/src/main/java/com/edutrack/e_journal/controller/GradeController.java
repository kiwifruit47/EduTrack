package com.edutrack.e_journal.controller;

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
import jakarta.validation.Valid;
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
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeRepository    gradeRepository;
    private final StudentRepository  studentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository     userRepository;

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<GradeDto> getByClass(@PathVariable Long classId) {
        return gradeRepository.findAllBySchedule_SchoolClass_Id(classId).stream()
                .map(this::toDto).toList();
    }

    @GetMapping("/student/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<GradeDto> getMyGrades(@AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        return gradeRepository.findAllByStudent_Id(user.getId()).stream()
                .map(this::toDto).toList();
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN','HEADMASTER','TEACHER')")
    public List<GradeDto> getByStudent(@PathVariable Long studentId) {
        return gradeRepository.findAllByStudent_Id(studentId).stream()
                .map(this::toDto).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','HEADMASTER')")
    public ResponseEntity<GradeDto> create(@Valid @RequestBody GradeRequest req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found"));
        Schedule schedule = scheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule not found"));
        Grade grade = Grade.builder()
                .student(student)
                .schedule(schedule)
                .value(req.getValue())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(gradeRepository.save(grade)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','HEADMASTER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!gradeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found");
        }
        gradeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------

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
