package com.edutrack.e_journal.repository;

import com.edutrack.e_journal.entity.Absence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbsenceRepository extends JpaRepository<Absence, Long> {
    List<Absence> findAllBySchedule_SchoolClass_Id(Long classId);
    List<Absence> findAllByStudent_Id(Long studentId);
}
