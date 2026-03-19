package com.edutrack.e_journal.repository;

public interface AdminRepository extends CrudRepository<AdminEntity, Integer> {

    Boolean existsByUsername(String username);

    AdminEntity findByUsername(String username);

}
