package com.edutrack.e_journal.repository;

import com.edutrack.e_journal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAllBySchool_Id(Long schoolId);
    List<Student> findAllBySchoolClass_Id(Long classId);
    List<Student> findAllBySchoolIsNull();
}
