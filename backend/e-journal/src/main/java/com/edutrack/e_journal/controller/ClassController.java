package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.SchoolClassDto;
import com.edutrack.e_journal.dto.UserSummaryDto;
import com.edutrack.e_journal.repository.ClassRepository;
import com.edutrack.e_journal.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "Query school classes and their students")
@SecurityRequirement(name = "bearerAuth")
public class ClassController {

    private final ClassRepository   classRepository;
    private final StudentRepository studentRepository;

    @Operation(summary = "List all classes", description = "Returns every class across all schools. Accessible by ADMIN, HEADMASTER, and TEACHER.")
    @ApiResponse(responseCode = "200", description = "Class list returned")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<SchoolClassDto> getAll() {
        return classRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Operation(summary = "Get a class by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Class found"),
        @ApiResponse(responseCode = "404", description = "Class not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER','PARENT','STUDENT')")
    public SchoolClassDto getById(
            @Parameter(description = "Class ID") @PathVariable Long id) {
        return classRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }

    @Operation(summary = "List students in a class", description = "Returns id and full name for every student enrolled in the class.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student list returned"),
        @ApiResponse(responseCode = "404", description = "Class not found")
    })
    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER','TEACHER')")
    public List<UserSummaryDto> getStudents(
            @Parameter(description = "Class ID") @PathVariable Long id) {
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
