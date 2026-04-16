package com.edutrack.e_journal.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final AbsenceRepository  absenceRepository;
    private final StudentRepository  studentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository     userRepository;

    public List<AbsenceDto> getByClass(Long classId) {
        return absenceRepository.findAllBySchedule_SchoolClass_Id(classId).stream()
                .map(this::toDto).toList();
    }

    public List<AbsenceDto> getByCurrentStudent(UserDetails principal) {
        User user = resolveUser(principal);
        return absenceRepository.findAllByStudent_Id(user.getId()).stream()
                .map(this::toDto).toList();
    }

    public List<AbsenceDto> getByStudent(Long studentId) {
        return absenceRepository.findAllByStudent_Id(studentId).stream()
                .map(this::toDto).toList();
    }

    public AbsenceDto create(AbsenceRequest req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found"));
        Schedule schedule = scheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schedule not found"));
        Absence absence = Absence.builder()
                .student(student)
                .schedule(schedule)
                .date(LocalDate.parse(req.getDate()))
                .build();
        return toDto(absenceRepository.save(absence));
    }

    public AbsenceDto toggleExcuse(Long id) {
        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Absence not found"));
        absence.setIsExcused(!absence.getIsExcused());
        return toDto(absenceRepository.save(absence));
    }

    public void delete(Long id) {
        if (!absenceRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Absence not found");
        absenceRepository.deleteById(id);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

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
