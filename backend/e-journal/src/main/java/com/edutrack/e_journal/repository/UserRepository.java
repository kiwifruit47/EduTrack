package com.edutrack.e_journal.repository;

import com.edutrack.e_journal.entity.RoleEnum;
import com.edutrack.e_journal.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByRole_Name(RoleEnum role);

    /** STUDENT-role users not yet enrolled in any school (no student record or school is null). */
    @Query("SELECT u FROM User u WHERE u.role.name = com.edutrack.e_journal.entity.RoleEnum.STUDENT " +
           "AND u.id NOT IN (SELECT s.id FROM Student s WHERE s.school IS NOT NULL)")
    List<User> findAvailableStudents();

    /** TEACHER-role users not yet assigned to any school (no teacher record or school is null). */
    @Query("SELECT u FROM User u WHERE u.role.name = com.edutrack.e_journal.entity.RoleEnum.TEACHER " +
           "AND u.id NOT IN (SELECT t.id FROM Teacher t WHERE t.school IS NOT NULL)")
    List<User> findAvailableTeachers();
}
