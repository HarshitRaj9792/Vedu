package org.example.videocall.repo;

import org.example.videocall.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface Student_repo extends JpaRepository<Student, UUID> {
    java.util.Optional<Student> findByEmail(String email);
}
