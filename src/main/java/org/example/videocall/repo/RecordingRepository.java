package org.example.videocall.repo;

import org.example.videocall.model.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordingRepository extends JpaRepository<Recording, Long> {
    List<Recording>     findByTeacherIdOrderByCreatedAtDesc(String teacherId);
    List<Recording>     findAllByOrderByCreatedAtDesc();
    Optional<Recording> findByEgressId(String egressId);
    Recording           findTopByRoomNameAndFileUrlOrderByCreatedAtDesc(String roomName, String fileUrl);

    // Only completed recordings (fileUrl is not empty)
    @Query("SELECT r FROM Recording r WHERE r.fileUrl <> '' ORDER BY r.createdAt DESC")
    List<Recording> findAllCompletedOrderByCreatedAtDesc();

    // Only completed recordings for a specific teacher
    @Query("SELECT r FROM Recording r WHERE r.teacherId = :teacherId AND r.fileUrl <> '' ORDER BY r.createdAt DESC")
    List<Recording> findCompletedByTeacherIdOrderByCreatedAtDesc(String teacherId);
}
