package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.SubjectDto;
import com.edutrack.e_journal.dto.SubjectRequest;
import com.edutrack.e_journal.entity.Subject;
import com.edutrack.e_journal.repository.SubjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Manage school subjects (admin only for write operations)")
@SecurityRequirement(name = "bearerAuth")
public class SubjectController {

    private final SubjectRepository subjectRepository;

    @Operation(summary = "List all subjects", description = "Returns every subject. Accessible by ADMIN and HEADMASTER.")
    @ApiResponse(responseCode = "200", description = "Subject list returned")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public List<SubjectDto> getAll() {
        return subjectRepository.findAll().stream()
                .map(s -> new SubjectDto(s.getId(), s.getName()))
                .toList();
    }

    @Operation(summary = "Create a subject", description = "Creates a new subject. ADMIN only.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Subject created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectDto> create(@Valid @RequestBody SubjectRequest req) {
        Subject saved = subjectRepository.save(Subject.builder().name(req.getName()).build());
        return ResponseEntity.status(HttpStatus.CREATED).body(new SubjectDto(saved.getId(), saved.getName()));
    }

    @Operation(summary = "Update a subject", description = "Renames an existing subject. ADMIN only.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subject updated"),
        @ApiResponse(responseCode = "404", description = "Subject not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectDto> update(
            @Parameter(description = "Subject ID") @PathVariable Long id,
            @Valid @RequestBody SubjectRequest req) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
        subject.setName(req.getName());
        Subject saved = subjectRepository.save(subject);
        return ResponseEntity.ok(new SubjectDto(saved.getId(), saved.getName()));
    }

    @Operation(summary = "Delete a subject", description = "Permanently deletes a subject. ADMIN only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Subject deleted"),
        @ApiResponse(responseCode = "404", description = "Subject not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Subject ID") @PathVariable Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found");
        }
        subjectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
