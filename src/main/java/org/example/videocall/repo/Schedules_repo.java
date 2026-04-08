package org.example.videocall.repo;

import org.example.videocall.model.Schedules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface Schedules_repo extends JpaRepository<Schedules, Long> {
    List<Schedules> findByTopicNameIgnoreCase(String topic);
    List<Schedules> findByTopicCourseAndTopicClassOrderByTopicTimeAscAllIgnoreCase(String course, String studentClass);
    List<Schedules> findByTeacheridOrderByTopicTimeAscAllIgnoreCase(UUID teacherid);

    // Auto-complete: mark UPCOMING/LIVE sessions older than cutoff as COMPLETED
    @Modifying
    @Query("UPDATE Schedules s SET s.status = 'COMPLETED' WHERE s.status IN ('UPCOMING','LIVE') AND s.topicTime < :cutoff")
    int autoCompleteOld(@Param("cutoff") LocalDateTime cutoff);
}
