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
public class ComplaintController {

    private final ComplaintRepository  complaintRepository;
    private final StudentRepository    studentRepository;
    private final ScheduleRepository   scheduleRepository;
    private final UserRepository       userRepository;

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<ComplaintDto> getByClass(@PathVariable Long classId) {
        return complaintRepository.findAllBySchedule_SchoolClass_Id(classId).stream()
                .map(this::toDto).toList();
    }

    @GetMapping("/student/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ComplaintDto> getMyComplaints(@AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        return complaintRepository.findAllByStudent_Id(user.getId()).stream()
                .map(this::toDto).toList();
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN','HEADMASTER','TEACHER')")
    public List<ComplaintDto> getByStudent(@PathVariable Long studentId) {
        return complaintRepository.findAllByStudent_Id(studentId).stream()
                .map(this::toDto).toList();
    }

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','HEADMASTER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
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
