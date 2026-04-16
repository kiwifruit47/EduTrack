package com.edutrack.e_journal.service;

import com.edutrack.e_journal.dto.SubjectDto;
import com.edutrack.e_journal.dto.SubjectRequest;
import com.edutrack.e_journal.entity.Subject;
import com.edutrack.e_journal.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public List<SubjectDto> getAll() {
        return subjectRepository.findAll().stream()
                .map(s -> new SubjectDto(s.getId(), s.getName()))
                .toList();
    }

    public SubjectDto create(SubjectRequest req) {
        Subject saved = subjectRepository.save(Subject.builder().name(req.getName()).build());
        return new SubjectDto(saved.getId(), saved.getName());
    }

    public SubjectDto update(Long id, SubjectRequest req) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
        subject.setName(req.getName());
        Subject saved = subjectRepository.save(subject);
        return new SubjectDto(saved.getId(), saved.getName());
    }

    public void delete(Long id) {
        if (!subjectRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found");
        subjectRepository.deleteById(id);
    }
}
