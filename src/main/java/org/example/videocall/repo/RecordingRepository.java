package org.example.videocall.repo;

import org.example.videocall.model.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordingRepository extends JpaRepository<Recording, Long> {
    List<Recording> findByTeacherIdOrderByCreatedAtDesc(String teacherId);
    List<Recording> findAllByOrderByCreatedAtDesc();
}
