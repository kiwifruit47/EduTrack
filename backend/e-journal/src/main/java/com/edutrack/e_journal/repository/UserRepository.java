package com.edutrack.e_journal.repository;

import com.edutrack.e_journal.entity.RoleEnum;
import com.edutrack.e_journal.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByRole_Name(RoleEnum role);

    List<User> findAllByChildren_School_Id(Long schoolId);
}
