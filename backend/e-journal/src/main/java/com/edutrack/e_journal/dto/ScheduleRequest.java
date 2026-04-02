package com.edutrack.e_journal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleRequest {

    @NotNull
    private Long classId;

    @NotNull
    private Long subjectId;

    @NotNull
    private Long teacherId;

    @NotNull @Min(1) @Max(2)
    private Integer term;

    @NotNull @Min(1) @Max(5)
    private Integer dayOfWeek;
}
