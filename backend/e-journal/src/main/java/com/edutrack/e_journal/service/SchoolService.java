package com.edutrack.e_journal.service;

import com.edutrack.e_journal.dto.SchoolDto;
import com.edutrack.e_journal.dto.SchoolRequest;
import com.edutrack.e_journal.dto.SchoolScheduleEntryDto;
import com.edutrack.e_journal.dto.SchoolScheduleEntryRequest;
import com.edutrack.e_journal.dto.SchoolTermConfigDto;
import com.edutrack.e_journal.entity.*;
import com.edutrack.e_journal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private static final SchoolTermConfigDto DEFAULT_TERM_CONFIG =
            new SchoolTermConfigDto("09-15", "02-01", "06-01", "06-15", "07-01");

    private final SchoolRepository              schoolRepository;
    private final UserRepository                userRepository;
    private final SchoolProfileRepository       profileRepository;
    private final SchoolScheduleEntryRepository scheduleEntryRepository;
    private final SchoolTermConfigRepository    termConfigRepository;

    // ── Schools ───────────────────────────────────────────────────────────────

    public List<SchoolDto> getAllSchools() {
        return schoolRepository.findAll().stream().map(this::toDto).toList();
    }

    public SchoolDto getSchoolById(Long id) {
        return schoolRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
    }

    public SchoolDto createSchool(SchoolRequest req) {
        School school = School.builder()
                .name(req.getName())
                .address(req.getAddress())
                .type(resolveType(req.getType()))
                .director(resolveHeadmasterById(req.getHeadmasterId()))
                .build();
        return toDto(schoolRepository.save(school));
    }

    public SchoolDto updateSchool(Long id, SchoolRequest req) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
        school.setName(req.getName());
        school.setAddress(req.getAddress());
        school.setType(resolveType(req.getType()));
        school.setDirector(resolveHeadmasterById(req.getHeadmasterId()));
        return toDto(schoolRepository.save(school));
    }

    public SchoolDto updateSchoolInfo(Long schoolId, String name, String address,
                                      UserDetails principal) {
        checkHeadmasterSchoolAccess(principal, schoolId);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
        school.setName(name);
        school.setAddress(address);
        return toDto(schoolRepository.save(school));
    }

    public void deleteSchool(Long id) {
        if (!schoolRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found");
        schoolRepository.deleteById(id);
    }

    // ── Profiles ──────────────────────────────────────────────────────────────

    public List<SchoolDto.ProfileDto> getProfiles(Long schoolId) {
        return profileRepository.findAllBySchool_Id(schoolId).stream()
                .map(p -> new SchoolDto.ProfileDto(p.getId(), p.getName()))
                .toList();
    }

    public SchoolDto.ProfileDto addProfile(Long schoolId, String name) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
        SchoolProfile profile = SchoolProfile.builder().name(name).school(school).build();
        SchoolProfile saved = profileRepository.save(profile);
        return new SchoolDto.ProfileDto(saved.getId(), saved.getName());
    }

    public void deleteProfile(Long profileId) {
        if (!profileRepository.existsById(profileId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
        profileRepository.deleteById(profileId);
    }

    // ── Daily Schedule ────────────────────────────────────────────────────────

    public List<SchoolScheduleEntryDto> getSchedule(Long schoolId) {
        return scheduleEntryRepository.findAllBySchool_IdOrderBySortOrder(schoolId).stream()
                .map(this::toScheduleDto).toList();
    }

    public SchoolScheduleEntryDto addScheduleEntry(Long schoolId, SchoolScheduleEntryRequest req,
                                                   UserDetails principal) {
        checkHeadmasterSchoolAccess(principal, schoolId);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
        int nextOrder = scheduleEntryRepository.findAllBySchool_IdOrderBySortOrder(schoolId).size();
        SchoolScheduleEntry entry = SchoolScheduleEntry.builder()
                .school(school)
                .type(resolveEntryType(req.getType()))
                .label(req.getLabel())
                .startTime(LocalTime.parse(req.getStartTime()))
                .endTime(LocalTime.parse(req.getEndTime()))
                .eventDate(parseDate(req.getEventDate()))
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : nextOrder)
                .build();
        return toScheduleDto(scheduleEntryRepository.save(entry));
    }

    public SchoolScheduleEntryDto updateScheduleEntry(Long entryId, SchoolScheduleEntryRequest req,
                                                      UserDetails principal) {
        SchoolScheduleEntry entry = scheduleEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule entry not found"));
        checkHeadmasterSchoolAccess(principal, entry.getSchool().getId());
        entry.setType(resolveEntryType(req.getType()));
        entry.setLabel(req.getLabel());
        entry.setStartTime(LocalTime.parse(req.getStartTime()));
        entry.setEndTime(LocalTime.parse(req.getEndTime()));
        entry.setEventDate(parseDate(req.getEventDate()));
        if (req.getSortOrder() != null) entry.setSortOrder(req.getSortOrder());
        return toScheduleDto(scheduleEntryRepository.save(entry));
    }

    public void deleteScheduleEntry(Long entryId, UserDetails principal) {
        SchoolScheduleEntry entry = scheduleEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule entry not found"));
        checkHeadmasterSchoolAccess(principal, entry.getSchool().getId());
        scheduleEntryRepository.deleteById(entryId);
    }

    // ── Term Config ───────────────────────────────────────────────────────────

    public SchoolTermConfigDto getTermConfig(Long schoolId) {
        return termConfigRepository.findById(schoolId)
                .map(c -> new SchoolTermConfigDto(c.getStartDate(), c.getTerm2Start(),
                        c.getElementaryEnd(), c.getProgymnasiumEnd(), c.getGymnasiumEnd()))
                .orElse(DEFAULT_TERM_CONFIG);
    }

    public SchoolTermConfigDto updateTermConfig(Long schoolId, SchoolTermConfigDto req,
                                                UserDetails principal) {
        checkHeadmasterSchoolAccess(principal, schoolId);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
        SchoolTermConfig config = termConfigRepository.findById(schoolId).orElse(
                SchoolTermConfig.builder().id(schoolId).school(school)
                        .startDate("09-15").term2Start("02-01")
                        .elementaryEnd("06-01").progymnasiumEnd("06-15").gymnasiumEnd("07-01")
                        .build());
        config.setStartDate(req.getStartDate());
        config.setTerm2Start(req.getTerm2Start());
        config.setElementaryEnd(req.getElementaryEnd());
        config.setProgymnasiumEnd(req.getProgymnasiumEnd());
        config.setGymnasiumEnd(req.getGymnasiumEnd());
        termConfigRepository.save(config);
        return req;
    }

    // ── Student Limit ─────────────────────────────────────────────────────────

    public void updateStudentLimit(Long schoolId, Integer limit, UserDetails principal) {
        checkHeadmasterSchoolAccess(principal, schoolId);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
        school.setStudentLimit(limit);
        schoolRepository.save(school);
    }

    // ── Shared helpers ─────────────────────────────────────────────────────────

    public void checkHeadmasterSchoolAccess(UserDetails principal, Long schoolId) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            User headmaster = userRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            School headmasterSchool = schoolRepository.findByDirector_Id(headmaster.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "No school assigned to this headmaster"));
            if (!headmasterSchool.getId().equals(schoolId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied to this school's schedule");
            }
        }
    }

    public School resolveHeadmasterSchool(User headmaster) {
        return schoolRepository.findByDirector_Id(headmaster.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No school assigned to this headmaster"));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private User resolveHeadmasterById(Long id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Headmaster user not found"));
    }

    private SchoolType resolveType(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return SchoolType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid school type: " + type);
        }
    }

    private ScheduleEntryType resolveEntryType(String type) {
        try {
            return ScheduleEntryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid entry type: " + type);
        }
    }

    private LocalDate parseDate(String date) {
        return (date != null && !date.isBlank()) ? LocalDate.parse(date) : null;
    }

    private SchoolDto toDto(School s) {
        String headmasterName = s.getDirector() != null
                ? s.getDirector().getFirstName() + " " + s.getDirector().getLastName()
                : null;
        String type = s.getType() != null ? s.getType().name() : null;
        List<SchoolDto.ProfileDto> profiles = profileRepository.findAllBySchool_Id(s.getId()).stream()
                .map(p -> new SchoolDto.ProfileDto(p.getId(), p.getName()))
                .toList();
        return new SchoolDto(s.getId(), s.getName(), s.getAddress(), type,
                headmasterName, profiles, s.getStudentLimit());
    }

    private SchoolScheduleEntryDto toScheduleDto(SchoolScheduleEntry e) {
        return new SchoolScheduleEntryDto(
                e.getId(),
                e.getType().name(),
                e.getLabel(),
                e.getStartTime().toString(),
                e.getEndTime().toString(),
                e.getEventDate() != null ? e.getEventDate().toString() : null,
                e.getSortOrder()
        );
    }
}
