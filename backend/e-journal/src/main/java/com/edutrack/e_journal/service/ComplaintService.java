package com.edutrack.e_journal.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository  complaintRepository;
    private final StudentRepository    studentRepository;
    private final ScheduleRepository   scheduleRepository;
    private final UserRepository       userRepository;

    public List<ComplaintDto> getByClass(Long classId) {
        return complaintRepository.findAllBySchedule_SchoolClass_Id(classId).stream()
                .map(this::toDto).toList();
    }

    public List<ComplaintDto> getByCurrentStudent(UserDetails principal) {
        User user = resolveUser(principal);
        return complaintRepository.findAllByStudent_Id(user.getId()).stream()
                .map(this::toDto).toList();
    }

    public List<ComplaintDto> getByStudent(Long studentId) {
        return complaintRepository.findAllByStudent_Id(studentId).stream()
                .map(this::toDto).toList();
    }

    public ComplaintDto create(ComplaintRequest req) {
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
        return toDto(complaintRepository.save(complaint));
    }

    public void delete(Long id) {
        if (!complaintRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found");
        complaintRepository.deleteById(id);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

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
