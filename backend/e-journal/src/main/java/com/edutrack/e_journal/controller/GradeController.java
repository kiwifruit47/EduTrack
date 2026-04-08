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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "Record and query student grades (Bulgarian scale 2–6)")
@SecurityRequirement(name = "bearerAuth")
public class GradeController {

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

    @Operation(summary = "List grades for a class", description = "Returns all grades for every student in the given class. Accessible by ADMIN, HEADMASTER, and TEACHER.")
    @ApiResponse(responseCode = "200", description = "Grade list returned")
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<GradeDto> getByClass(
            @Parameter(description = "Class ID") @PathVariable Long classId) {
        return gradeRepository.findAllBySchedule_SchoolClass_Id(classId).stream()
                .map(this::toDto).toList();
    }

    @Operation(summary = "Get my grades", description = "Returns grades for the authenticated student.")
    @ApiResponse(responseCode = "200", description = "Grade list returned")
    @GetMapping("/student/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<GradeDto> getMyGrades(@AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        return gradeRepository.findAllByStudent_Id(user.getId()).stream()
                .map(this::toDto).toList();
    }

    @Operation(summary = "List grades for a student", description = "Returns all grades for a specific student. Accessible by PARENT, ADMIN, HEADMASTER, and TEACHER.")
    @ApiResponse(responseCode = "200", description = "Grade list returned")
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN','HEADMASTER','TEACHER')")
    public List<GradeDto> getByStudent(
            @Parameter(description = "Student user ID") @PathVariable Long studentId) {
        return gradeRepository.findAllByStudent_Id(studentId).stream()
                .map(this::toDto).toList();
    }

    @Operation(summary = "Add a grade", description = "Records a new grade. Value must be one of: 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5, 6. Accessible by TEACHER, ADMIN, and HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Grade recorded"),
        @ApiResponse(responseCode = "400", description = "Invalid grade value, student, or schedule ID")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','HEADMASTER')")
    public ResponseEntity<GradeDto> create(@Valid @RequestBody GradeRequest req) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(gradeRepository.save(grade)));
    }

    @Operation(summary = "Delete a grade", description = "Permanently removes a grade record. Accessible by TEACHER, ADMIN, and HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Grade deleted"),
        @ApiResponse(responseCode = "404", description = "Grade not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','HEADMASTER')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Grade ID") @PathVariable Long id) {
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
