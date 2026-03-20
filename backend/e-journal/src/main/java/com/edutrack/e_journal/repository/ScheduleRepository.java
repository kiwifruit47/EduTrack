package com.edutrack.e_journal.repository;

import com.edutrack.e_journal.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllBySchoolClass_Id(Long classId);
    List<Schedule> findAllByTeacher_Id(Long teacherId);
}
