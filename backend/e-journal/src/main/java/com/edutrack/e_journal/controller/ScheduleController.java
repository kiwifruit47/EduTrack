package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.ScheduleDto;
import com.edutrack.e_journal.dto.ScheduleRequest;
import com.edutrack.e_journal.entity.*;
import com.edutrack.e_journal.repository.*;
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

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedules", description = "Curriculum schedules — which teacher teaches which subject to which class")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;
    private final ClassRepository    classRepository;
    private final SubjectRepository  subjectRepository;
    private final TeacherRepository  teacherRepository;
    private final UserRepository     userRepository;

    @Operation(summary = "List schedules for a class", description = "Returns all curriculum entries for the given class. Accessible by all authenticated roles.")
    @ApiResponse(responseCode = "200", description = "Schedule list returned")
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER','PARENT','STUDENT')")
    public List<ScheduleDto> getByClass(
            @Parameter(description = "Class ID") @PathVariable Long classId) {
        return scheduleRepository.findAllBySchoolClass_Id(classId).stream()
                .map(this::toDto)
                .toList();
    }

    @Operation(summary = "Get my schedule", description = "Returns the full curriculum schedule for the currently authenticated teacher.")
    @ApiResponse(responseCode = "200", description = "Teacher's schedule returned")
    @GetMapping("/teacher/me")
    @PreAuthorize("hasRole('TEACHER')")
    public List<ScheduleDto> getMySchedule(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return scheduleRepository.findAllByTeacher_Id(user.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Operation(summary = "Create a schedule entry", description = "Assigns a teacher to teach a subject to a class for a given term. Accessible by ADMIN and HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Schedule entry created"),
        @ApiResponse(responseCode = "400", description = "Invalid class, subject, or teacher ID")
    })
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
                .dayOfWeek(req.getDayOfWeek())
                .startTime(LocalTime.parse(req.getStartTime()))
                .endTime(LocalTime.parse(req.getEndTime()))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(scheduleRepository.save(schedule)));
    }

    @Operation(summary = "Delete a schedule entry", description = "Removes a curriculum entry. Accessible by ADMIN and HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Schedule entry deleted"),
        @ApiResponse(responseCode = "404", description = "Schedule entry not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Schedule entry ID") @PathVariable Long id) {
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
                s.getTerm(),
                s.getDayOfWeek(),
                s.getStartTime().toString(),
                s.getEndTime().toString()
        );
    }
}
