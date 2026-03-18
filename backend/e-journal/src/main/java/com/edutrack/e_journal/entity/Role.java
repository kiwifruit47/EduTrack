package com.edutrack.e_journal.entity;

import jakarta.persistence.*;
        import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleName name;
}

enum RoleName {
    ROLE_ADMIN,
    ROLE_TEACHER,
    ROLE_STUDENT,
    ROLE_PARENT
}
