package com.edutrack.e_journal.repository;

import com.edutrack.e_journal.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findAllBySchedule_SchoolClass_Id(Long classId);
    List<Grade> findAllByStudent_Id(Long studentId);
}
