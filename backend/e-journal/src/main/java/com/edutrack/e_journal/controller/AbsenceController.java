package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.AbsenceDto;
import com.edutrack.e_journal.dto.AbsenceRequest;
import com.edutrack.e_journal.entity.Absence;
import com.edutrack.e_journal.entity.Schedule;
import com.edutrack.e_journal.entity.Student;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.AbsenceRepository;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceRepository  absenceRepository;
    private final StudentRepository  studentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository     userRepository;

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<AbsenceDto> getByClass(@PathVariable Long classId) {
        return absenceRepository.findAllBySchedule_SchoolClass_Id(classId).stream()
                .map(this::toDto).toList();
    }

    @GetMapping("/student/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<AbsenceDto> getMyAbsences(@AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        return absenceRepository.findAllByStudent_Id(user.getId()).stream()
                .map(this::toDto).toList();
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN','HEADMASTER','TEACHER')")
    public List<AbsenceDto> getByStudent(@PathVariable Long studentId) {
        return absenceRepository.findAllByStudent_Id(studentId).stream()
                .map(this::toDto).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','HEADMASTER')")
    public ResponseEntity<AbsenceDto> create(@Valid @RequestBody AbsenceRequest req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found"));
        Schedule schedule = scheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule not found"));
        Absence absence = Absence.builder()
                .student(student)
                .schedule(schedule)
                .date(LocalDate.parse(req.getDate()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(absenceRepository.save(absence)));
    }

    @PutMapping("/{id}/excuse")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','HEADMASTER')")
    public ResponseEntity<AbsenceDto> toggleExcuse(@PathVariable Long id) {
        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Absence not found"));
        absence.setIsExcused(!absence.getIsExcused());
        return ResponseEntity.ok(toDto(absenceRepository.save(absence)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','HEADMASTER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!absenceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Absence not found");
        }
        absenceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private AbsenceDto toDto(Absence a) {
        String studentName = a.getStudent().getUser().getFirstName()
                + " " + a.getStudent().getUser().getLastName();
        String teacherName = a.getSchedule().getTeacher().getUser().getFirstName()
                + " " + a.getSchedule().getTeacher().getUser().getLastName();
        return new AbsenceDto(
                a.getId(),
                a.getStudent().getId(),
                studentName,
                a.getSchedule().getId(),
                a.getSchedule().getSubject().getId(),
                a.getSchedule().getSubject().getName(),
                teacherName,
                a.getSchedule().getTerm(),
                a.getDate().toString(),
                a.getIsExcused()
        );
    }
}
