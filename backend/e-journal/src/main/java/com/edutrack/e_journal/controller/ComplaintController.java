package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.ComplaintDto;
import com.edutrack.e_journal.dto.ComplaintRequest;
import com.edutrack.e_journal.entity.Complaint;
import com.edutrack.e_journal.entity.Schedule;
import com.edutrack.e_journal.entity.Student;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.ComplaintRepository;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Record and query disciplinary complaints against students")
@SecurityRequirement(name = "bearerAuth")
public class ComplaintController {

    private final ComplaintRepository  complaintRepository;
    private final StudentRepository    studentRepository;
    private final ScheduleRepository   scheduleRepository;
    private final UserRepository       userRepository;

    @Operation(summary = "List complaints for a class", description = "Returns all complaints for every student in the given class. Accessible by ADMIN, HEADMASTER, and TEACHER.")
    @ApiResponse(responseCode = "200", description = "Complaint list returned")
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<ComplaintDto> getByClass(
            @Parameter(description = "Class ID") @PathVariable Long classId) {
        return complaintRepository.findAllBySchedule_SchoolClass_Id(classId).stream()
                .map(this::toDto).toList();
    }

    @Operation(summary = "Get my complaints", description = "Returns complaints issued against the authenticated student.")
    @ApiResponse(responseCode = "200", description = "Complaint list returned")
    @GetMapping("/student/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ComplaintDto> getMyComplaints(@AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        return complaintRepository.findAllByStudent_Id(user.getId()).stream()
                .map(this::toDto).toList();
    }

    @Operation(summary = "List complaints for a student", description = "Returns all complaints for a specific student. Accessible by PARENT, ADMIN, HEADMASTER, and TEACHER.")
    @ApiResponse(responseCode = "200", description = "Complaint list returned")
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN','HEADMASTER','TEACHER')")
    public List<ComplaintDto> getByStudent(
            @Parameter(description = "Student user ID") @PathVariable Long studentId) {
        return complaintRepository.findAllByStudent_Id(studentId).stream()
                .map(this::toDto).toList();
    }

    @Operation(summary = "File a complaint", description = "Creates a new disciplinary complaint against a student. Accessible by TEACHER, ADMIN, and HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Complaint recorded"),
        @ApiResponse(responseCode = "400", description = "Invalid student or schedule ID")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','HEADMASTER')")
    public ResponseEntity<ComplaintDto> create(@Valid @RequestBody ComplaintRequest req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found"));
        Schedule schedule = scheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule not found"));
        Complaint complaint = Complaint.builder()
                .student(student)
                .schedule(schedule)
                .description(req.getDescription())
                .date(LocalDate.parse(req.getDate()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(complaintRepository.save(complaint)));
    }

    @Operation(summary = "Delete a complaint", description = "Permanently removes a complaint record. Accessible by TEACHER, ADMIN, and HEADMASTER.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Complaint deleted"),
        @ApiResponse(responseCode = "404", description = "Complaint not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','HEADMASTER')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Complaint ID") @PathVariable Long id) {
        if (!complaintRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found");
        }
        complaintRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private ComplaintDto toDto(Complaint c) {
        String studentName = c.getStudent().getUser().getFirstName()
                + " " + c.getStudent().getUser().getLastName();
        String teacherName = c.getSchedule().getTeacher().getUser().getFirstName()
                + " " + c.getSchedule().getTeacher().getUser().getLastName();
        return new ComplaintDto(
                c.getId(),
                c.getStudent().getId(),
                studentName,
                c.getSchedule().getId(),
                c.getSchedule().getSubject().getId(),
                c.getSchedule().getSubject().getName(),
                teacherName,
                c.getSchedule().getTerm(),
                c.getDate().toString(),
                c.getDescription()
        );
    }
}
