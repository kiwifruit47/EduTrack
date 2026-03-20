package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.ScheduleDto;
import com.edutrack.e_journal.dto.ScheduleRequest;
import com.edutrack.e_journal.entity.*;
import com.edutrack.e_journal.repository.*;
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
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;
    private final ClassRepository    classRepository;
    private final SubjectRepository  subjectRepository;
    private final TeacherRepository  teacherRepository;
    private final UserRepository     userRepository;

    /** All schedules for a given class, used by the class schedule view. */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER','PARENT','STUDENT')")
    public List<ScheduleDto> getByClass(@PathVariable Long classId) {
        return scheduleRepository.findAllBySchoolClass_Id(classId).stream()
                .map(this::toDto)
                .toList();
    }

    /** All schedules for the currently authenticated teacher. */
    @GetMapping("/teacher/me")
    @PreAuthorize("hasRole('TEACHER')")
    public List<ScheduleDto> getMySchedule(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return scheduleRepository.findAllByTeacher_Id(user.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ResponseEntity<ScheduleDto> create(@Valid @RequestBody ScheduleRequest req) {
        SchoolClass schoolClass = classRepository.findById(req.getClassId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class not found"));
        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject not found"));
        Teacher teacher = teacherRepository.findById(req.getTeacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teacher not found"));

        Schedule schedule = Schedule.builder()
                .school(schoolClass.getSchool())
                .schoolClass(schoolClass)
                .subject(subject)
                .teacher(teacher)
                .term(req.getTerm())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(scheduleRepository.save(schedule)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule entry not found");
        }
        scheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------

    private ScheduleDto toDto(Schedule s) {
        String teacherName = s.getTeacher().getUser().getFirstName()
                + " " + s.getTeacher().getUser().getLastName();
        return new ScheduleDto(
                s.getId(),
                s.getSubject().getId(),
                s.getSubject().getName(),
                s.getTeacher().getId(),
                teacherName,
                s.getSchoolClass().getId(),
                s.getSchoolClass().getName(),
                s.getSchool().getName(),
                s.getTerm()
        );
    }
}
