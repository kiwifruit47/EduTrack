package com.edutrack.e_journal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScheduleDto {
    private Long    id;
    private Long    subjectId;
    private String  subjectName;
    private Long    teacherId;
    private String  teacherName;
    private Long    classId;
    private String  className;
    private String  schoolName;
    private Integer term;
    private Integer dayOfWeek;
}
