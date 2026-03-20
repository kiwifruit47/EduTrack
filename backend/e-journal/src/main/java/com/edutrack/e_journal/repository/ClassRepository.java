package com.edutrack.e_journal.repository;

import com.edutrack.e_journal.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRepository extends JpaRepository<SchoolClass, Long> {
    List<SchoolClass> findAllBySchool_Id(Long schoolId);
}
