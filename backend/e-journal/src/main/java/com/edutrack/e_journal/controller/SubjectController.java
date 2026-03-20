package com.edutrack.e_journal.controller;

import com.edutrack.e_journal.dto.SubjectDto;
import com.edutrack.e_journal.dto.SubjectRequest;
import com.edutrack.e_journal.entity.Subject;
import com.edutrack.e_journal.repository.SubjectRepository;
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
public class SubjectController {

    private final SubjectRepository subjectRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HEADMASTER')")
    public List<SubjectDto> getAll() {
        return subjectRepository.findAll().stream()
                .map(s -> new SubjectDto(s.getId(), s.getName()))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectDto> create(@Valid @RequestBody SubjectRequest req) {
        Subject saved = subjectRepository.save(Subject.builder().name(req.getName()).build());
        return ResponseEntity.status(HttpStatus.CREATED).body(new SubjectDto(saved.getId(), saved.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectDto> update(@PathVariable Long id, @Valid @RequestBody SubjectRequest req) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
        subject.setName(req.getName());
        Subject saved = subjectRepository.save(subject);
        return ResponseEntity.ok(new SubjectDto(saved.getId(), saved.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found");
        }
        subjectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
