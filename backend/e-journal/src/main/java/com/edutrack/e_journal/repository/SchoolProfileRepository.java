package com.edutrack.e_journal.repository;

import com.edutrack.e_journal.entity.SchoolProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolProfileRepository extends JpaRepository<SchoolProfile, Long> {
    List<SchoolProfile> findAllBySchool_Id(Long schoolId);
}
