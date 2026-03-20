package com.edutrack.e_journal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDto {
    private Long   id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
