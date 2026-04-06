package org.example.videocall.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "class_recordings")
@Data
@NoArgsConstructor
public class Recording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomName;

    @Column(nullable = false)
    private String teacherId;

    @Column(nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "egress_id", unique = true)
    private String egressId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
