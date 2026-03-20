package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.SchoolClassDto;
import com.edutrack.e_journal.dto.UserSummaryDto;
import com.edutrack.e_journal.repository.ClassRepository;
import com.edutrack.e_journal.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassRepository   classRepository;
    private final StudentRepository studentRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<SchoolClassDto> getAll() {
        return classRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER','PARENT','STUDENT')")
    public SchoolClassDto getById(@PathVariable Long id) {
        return classRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<UserSummaryDto> getStudents(@PathVariable Long id) {
        return studentRepository.findAllBySchoolClass_Id(id).stream()
                .map(s -> new UserSummaryDto(
                        s.getId(),
                        s.getUser().getFirstName() + " " + s.getUser().getLastName()))
                .toList();
    }

    private SchoolClassDto toDto(com.edutrack.e_journal.entity.SchoolClass c) {
        return new SchoolClassDto(c.getId(), c.getName(), c.getSchoolYear(),
                c.getSchool().getId(), c.getSchool().getName());
    }
}
