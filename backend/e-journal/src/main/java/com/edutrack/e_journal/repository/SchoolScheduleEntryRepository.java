package com.edutrack.e_journal.repository;

import com.edutrack.e_journal.entity.SchoolScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolScheduleEntryRepository extends JpaRepository<SchoolScheduleEntry, Long> {
    List<SchoolScheduleEntry> findAllBySchool_IdOrderBySortOrder(Long schoolId);
}
