package org.example.videocall.repo;

import org.example.videocall.model.Schedules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface Schedules_repo extends JpaRepository<Schedules, Long> {
    List<Schedules> findByTopicNameIgnoreCase(String topic);
    List<Schedules> findByTopicCourseAndTopicClassOrderByTopicTimeAscAllIgnoreCase(String course, String studentClass);
    List<Schedules> findByTeacheridOrderByTopicTimeAscAllIgnoreCase(UUID teacherid);
}
