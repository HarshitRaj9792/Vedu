package org.example.videocall.repo;

import org.example.videocall.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface Teacher_repo extends JpaRepository<Teacher, UUID> {
    java.util.Optional<Teacher> findByEmail(String email);


}
