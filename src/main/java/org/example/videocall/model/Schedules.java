package org.example.videocall.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Schedules")
@Data
public class Schedules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID teacherid;
    private String topicName;
    private String topicCourse;
    private String topicClass;
    private LocalDateTime topicTime;
    private String teacherName;
    private String status = "UPCOMING";
}