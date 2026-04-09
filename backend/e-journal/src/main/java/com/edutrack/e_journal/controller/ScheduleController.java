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

import com.edutrack.e_journal.entity.LectureType;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

        LectureType lectureType = req.getLectureType() != null ? req.getLectureType() : LectureType.STANDARD;
        boolean trackAttendance = req.getTrackAttendance() != null ? req.getTrackAttendance() : true;

        Schedule schedule = Schedule.builder()
                .school(schoolClass.getSchool())
                .schoolClass(schoolClass)
                .subject(subject)
                .teacher(teacher)
                .term(req.getTerm())
                .dayOfWeek(req.getDayOfWeek())
                .startTime(LocalTime.parse(req.getStartTime()))
                .endTime(LocalTime.parse(req.getEndTime()))
                .lectureType(lectureType)
                .trackAttendance(trackAttendance)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(scheduleRepository.save(schedule)));
    }

    @Operation(summary = "Update lecture type and attendance tracking",
               description = "Changes the lecture type (STANDARD / SIP / EXTRACURRICULAR) and whether absences are tracked. ADMIN and HEADMASTER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entry updated"),
        @ApiResponse(responseCode = "404", description = "Entry not found")
    })
    @PatchMapping("/{id}/type")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public ResponseEntity<ScheduleDto> patchType(
            @Parameter(description = "Schedule entry ID") @PathVariable Long id,
            @RequestBody TypePatchRequest req) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule entry not found"));
        if (req.getLectureType() != null)   s.setLectureType(req.getLectureType());
        if (req.getTrackAttendance() != null) s.setTrackAttendance(req.getTrackAttendance());
        return ResponseEntity.ok(toDto(scheduleRepository.save(s)));
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
                s.getEndTime().toString(),
                s.getLectureType().name(),
                s.getTrackAttendance()
        );
    }

    // ── Inline request DTO ────────────────────────────────────────────────────

    @Getter @NoArgsConstructor
    public static class TypePatchRequest {
        private LectureType lectureType;
        private Boolean trackAttendance;
    }
}
