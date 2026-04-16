package com.edutrack.e_journal.service;

import com.edutrack.e_journal.dto.ScheduleDto;
import com.edutrack.e_journal.dto.ScheduleRequest;
import com.edutrack.e_journal.entity.*;
import com.edutrack.e_journal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ClassRepository    classRepository;
    private final SubjectRepository  subjectRepository;
    private final TeacherRepository  teacherRepository;
    private final UserRepository     userRepository;

    public List<ScheduleDto> getByClass(Long classId) {
        return scheduleRepository.findAllBySchoolClass_Id(classId).stream()
                .map(this::toDto).toList();
    }

    public List<ScheduleDto> getMySchedule(UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return scheduleRepository.findAllByTeacher_Id(user.getId()).stream()
                .map(this::toDto).toList();
    }

    public ScheduleDto create(ScheduleRequest req) {
        SchoolClass schoolClass = classRepository.findById(req.getClassId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class not found"));
        Subject subject = subjectRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject not found"));
        Teacher teacher = teacherRepository.findById(req.getTeacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teacher not found"));

        LectureType lectureType   = req.getLectureType() != null ? req.getLectureType() : LectureType.STANDARD;
        boolean trackAttendance   = req.getTrackAttendance() != null ? req.getTrackAttendance() : true;

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

        return toDto(scheduleRepository.save(schedule));
    }

    public ScheduleDto patchType(Long id, LectureType lectureType, Boolean trackAttendance) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule entry not found"));
        if (lectureType != null)     s.setLectureType(lectureType);
        if (trackAttendance != null) s.setTrackAttendance(trackAttendance);
        return toDto(scheduleRepository.save(s));
    }

    public void delete(Long id) {
        if (!scheduleRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule entry not found");
        scheduleRepository.deleteById(id);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

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
}
