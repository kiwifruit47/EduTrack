package com.edutrack.e_journal.repository;

import com.edutrack.e_journal.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findAllBySchool_Id(Long schoolId);
}
